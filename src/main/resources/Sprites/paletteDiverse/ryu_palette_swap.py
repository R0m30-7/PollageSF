"""
Palette Swap per Sprite Sheet a Colori Piatti (stile SNES)
=============================================================
Sfrutta le "Alternate Palettes" gia' presenti nello sprite sheet (una posa
di riferimento ripetuta in piu' colori) per costruire una tabella di
conversione colore-per-colore ESATTA, poi la generalizza e la applica a
TUTTE le animazioni dell'atlas (non solo alla posa di riferimento).

Perche' funziona:
- Gli sprite SNES usano una palette molto limitata (qui: 16 colori per il
  costume). Le "Alternate Palettes" sono la STESSA identica posa ridisegnata
  con un'altra palette: confrontando pixel-per-pixel possiamo recuperare
  esattamente quale colore diventa quale, senza nessuna ambiguita'.
- Quella singola posa pero' non contiene TUTTE le sfumature usate nelle
  altre 200+ pose (es. un'ombra del gi visibile solo in un calcio alto).
  Per generalizzare, ogni colore non ancora noto viene mappato sul colore
  noto piu' vicino (distanza euclidea RGB) SOLO se la distanza e' sotto
  una soglia: oltre quella soglia il colore resta invariato (e' quasi
  certamente un elemento non ricolorabile: fuoco, ombra a terra, outline).

Uso:
    python3 palette_swap.py

Richiede nella stessa cartella:
    ryu_chromakey.png          (o ken_chromakey.png)
    ryu_atlas_manifest.json
"""
import json
import os
import zipfile

import numpy as np
from PIL import Image

ATLAS_PATH = "ryu_chromakey.png"
MANIFEST_PATH = "ryu_atlas_manifest.json"
OUTPUT_PREFIX = "ryu"

# Sezione dello sprite sheet che contiene le varianti di palette gia' pronte
# (stessa posa, colori diversi). Su Ryu/Ken e' row7, col3.
PALETTE_SECTION_ROW = 7
PALETTE_SECTION_COL = 3

# Frame che rappresenta il costume DI BASE (bianco/originale) nella STESSA
# identica posa delle varianti sopra. Su Ryu/Ken e' l'Idle, frame00
# (row0, col0) - verificato pixel-per-pixel identico alla sagoma delle
# varianti di palette.
BASE_REFERENCE_KEY = "row00_col00_frame00"
# Nomi delle varianti nell'ordine in cui compaiono nella sezione (frame00, 01, ...)
VARIANT_NAMES = ["gray", "orange", "blue", "lightblue", "lime", "maroon", "darkgreen"]

# Distanza RGB massima per estendere la mappatura a colori mai visti nella
# posa di riferimento. Piu' alta = piu' copertura ma rischio di toccare
# elementi che non fanno parte del costume (fuoco, ombra, outline).
MATCH_THRESHOLD = 35


def load_manifest():
    with open(MANIFEST_PATH) as f:
        return json.load(f)


def extract_reference_luts(atlas_arr, manifest):
    """Confronta la posa 'frame00' della sezione palette (presa come base)
    con ciascuna delle altre varianti, pixel per pixel, per costruire la
    mappatura esatta colore->colore per ogni variante."""
    frames = manifest["frames"]
    base_key = BASE_REFERENCE_KEY
    base_rect = frames[base_key]["frame"]
    bx, by, bw, bh = base_rect["x"], base_rect["y"], base_rect["w"], base_rect["h"]
    base_crop = atlas_arr[by:by + bh, bx:bx + bw]
    base_mask = base_crop[:, :, 3] > 0

    luts = {}
    for i, vname in enumerate(VARIANT_NAMES):
        key = f"row{PALETTE_SECTION_ROW:02d}_col{PALETTE_SECTION_COL:02d}_frame{i:02d}"
        rect = frames[key]["frame"]
        vx, vy, vw, vh = rect["x"], rect["y"], rect["w"], rect["h"]
        v_crop = atlas_arr[vy:vy + vh, vx:vx + vw]

        v_mask = v_crop[:, :, 3] > 0
        if v_crop.shape[:2] != base_crop.shape[:2] or not np.array_equal(base_mask, v_mask):
            print(f"ATTENZIONE: la sagoma di '{vname}' non combacia esattamente "
                  f"con il frame di riferimento {base_key}. La mappatura per "
                  f"questa variante potrebbe essere imprecisa.")

        lut = {}
        ys, xs = np.where(base_mask)
        for y, x in zip(ys, xs):
            src = tuple(base_crop[y, x, :3])
            dst = tuple(v_crop[y, x, :3])
            lut[src] = dst
        luts[vname] = lut
    return luts


def build_extended_lut(atlas_arr, base_lut, threshold):
    """Estende la LUT esatta con una mappatura 'nearest neighbor' per i
    colori presenti nell'atlas ma mai visti nella posa di riferimento."""
    known_colors = list(base_lut.keys())
    known_rgb = np.array(known_colors)

    mask = atlas_arr[:, :, 3] > 0
    all_colors = set(map(tuple, atlas_arr[mask][:, :3]))

    extended = dict(base_lut)
    for c in all_colors:
        if c in extended:
            continue
        dists = np.sqrt(((known_rgb.astype(int) - np.array(c).astype(int)) ** 2).sum(axis=1))
        idx = dists.argmin()
        if dists[idx] < threshold:
            extended[c] = base_lut[known_colors[idx]]
        # altrimenti non lo aggiungo: il colore restera' invariato
    return extended


def apply_lut(atlas_arr, lut):
    out = atlas_arr.copy()
    for src, dst in lut.items():
        m = (
            (atlas_arr[:, :, 0] == src[0]) &
            (atlas_arr[:, :, 1] == src[1]) &
            (atlas_arr[:, :, 2] == src[2]) &
            (atlas_arr[:, :, 3] > 0)
        )
        out[:, :, 0][m] = dst[0]
        out[:, :, 1][m] = dst[1]
        out[:, :, 2][m] = dst[2]
    return out


def export_variant(atlas_img_arr, manifest, variant_name):
    out_atlas_path = f"{OUTPUT_PREFIX}_{variant_name}_chromakey.png"
    Image.fromarray(atlas_img_arr).save(out_atlas_path)

    sprite_dir = f"{OUTPUT_PREFIX}_{variant_name}_sprites"
    os.makedirs(sprite_dir, exist_ok=True)
    img = Image.fromarray(atlas_img_arr)
    for name, info in manifest["frames"].items():
        r = info["frame"]
        crop = img.crop((r["x"], r["y"], r["x"] + r["w"], r["y"] + r["h"]))
        crop.save(f"{sprite_dir}/{name}.png")

    zip_path = f"{sprite_dir}.zip"
    with zipfile.ZipFile(zip_path, "w", zipfile.ZIP_DEFLATED) as zf:
        for fname in os.listdir(sprite_dir):
            zf.write(os.path.join(sprite_dir, fname), arcname=fname)

    return out_atlas_path, zip_path


def main():
    manifest = load_manifest()
    atlas = Image.open(ATLAS_PATH).convert("RGBA")
    atlas_arr = np.array(atlas)

    print("Estraggo le mappature colore esatte dalle Alternate Palettes...")
    reference_luts = extract_reference_luts(atlas_arr, manifest)

    for vname in VARIANT_NAMES:
        print(f"\n--- Variante: {vname} ---")
        ext_lut = build_extended_lut(atlas_arr, reference_luts[vname], MATCH_THRESHOLD)
        print(f"Colori mappati: {len(ext_lut)}")
        recolored = apply_lut(atlas_arr, ext_lut)
        atlas_path, zip_path = export_variant(recolored, manifest, vname)
        print(f"Salvato: {atlas_path}, {zip_path}")


if __name__ == "__main__":
    main()
