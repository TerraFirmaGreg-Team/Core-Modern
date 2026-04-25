from __future__ import annotations

import binascii
import os
import struct
import zipfile
import zlib
from dataclasses import dataclass
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
OUT_DIR = ROOT / "src/main/resources/assets/tfg/textures/block/wood/rope_ladder"

TFC_WOODS = [
    "acacia", "ash", "aspen", "birch", "blackwood", "chestnut", "douglas_fir",
    "hickory", "kapok", "mangrove", "maple", "oak", "palm", "pine",
    "rosewood", "sequoia", "spruce", "sycamore", "white_cedar", "willow",
]
AFC_WOODS = [
    "baobab", "eucalyptus", "mahogany", "hevea", "tualang", "teak",
    "cypress", "fig", "ironwood", "ipe",
]
BENEATH_WOODS = ["crimson", "warped"]
AD_ASTRA_WOODS = ["aeronos", "strophar", "glacian"]
TFG_WOODS = ["araucaria", "beech", "mahoe"]

FALLBACK_COLORS = {
    "acacia": (122, 75, 43), "ash": (168, 139, 96), "aspen": (192, 176, 119),
    "birch": (196, 181, 124), "blackwood": (42, 35, 29), "chestnut": (119, 74, 42),
    "douglas_fir": (126, 80, 45), "hickory": (135, 100, 55), "kapok": (190, 137, 66),
    "mangrove": (112, 72, 50), "maple": (161, 107, 59), "oak": (136, 103, 60),
    "palm": (180, 151, 84), "pine": (143, 111, 61), "rosewood": (103, 43, 49),
    "sequoia": (131, 56, 35), "spruce": (92, 64, 42), "sycamore": (175, 132, 76),
    "white_cedar": (183, 171, 108), "willow": (145, 128, 72),
    "baobab": (168, 108, 58), "eucalyptus": (150, 91, 71), "mahogany": (113, 52, 35),
    "hevea": (185, 142, 85), "tualang": (172, 119, 55), "teak": (142, 87, 42),
    "cypress": (139, 116, 72), "fig": (151, 103, 65), "ironwood": (72, 56, 45),
    "ipe": (91, 50, 70), "crimson": (119, 42, 68), "warped": (50, 112, 105),
    "aeronos": (134, 155, 91), "strophar": (92, 74, 132), "glacian": (132, 174, 190),
    "araucaria": (166, 128, 88), "beech": (187, 151, 98), "mahoe": (102, 119, 153),
    "ginkgo": (184, 151, 67),
}

ROPE_LIGHT = (190, 161, 108, 255)
ROPE_BASE = (143, 110, 69, 255)
ROPE_DARK = (88, 63, 39, 255)


@dataclass(frozen=True)
class TextureSource:
    wood: str
    namespace: str
    path: str

    @property
    def asset_path(self) -> str:
        return f"assets/{self.namespace}/textures/{self.path}.png"


def texture_sources() -> list[TextureSource]:
    sources: list[TextureSource] = []
    sources.extend(TextureSource(wood, "tfg", f"block/wood/planks/{wood}") for wood in TFG_WOODS)
    sources.extend(TextureSource(wood, "ad_astra", f"block/{wood}_planks") for wood in AD_ASTRA_WOODS)
    sources.append(TextureSource("ginkgo", "wan_ancient_beasts", "block/ginkgo_planks"))
    sources.extend(TextureSource(wood, "afc", f"block/wood/planks/{wood}") for wood in AFC_WOODS)
    sources.extend(TextureSource(wood, "beneath", f"block/wood/planks/{wood}") for wood in BENEATH_WOODS)
    sources.extend(TextureSource(wood, "tfc", f"block/wood/planks/{wood}") for wood in TFC_WOODS)
    return sources


def read_png(data: bytes) -> tuple[int, int, list[tuple[int, int, int, int]]]:
    if not data.startswith(b"\x89PNG\r\n\x1a\n"):
        raise ValueError("Not a PNG file")

    pos = 8
    width = height = bit_depth = color_type = 0
    palette: list[tuple[int, int, int]] = []
    transparency: list[int] = []
    compressed = bytearray()

    while pos < len(data):
        length = struct.unpack(">I", data[pos:pos + 4])[0]
        chunk_type = data[pos + 4:pos + 8]
        chunk_data = data[pos + 8:pos + 8 + length]
        pos += 12 + length

        if chunk_type == b"IHDR":
            width, height, bit_depth, color_type, _, _, _ = struct.unpack(">IIBBBBB", chunk_data)
        elif chunk_type == b"PLTE":
            palette = [tuple(chunk_data[i:i + 3]) for i in range(0, len(chunk_data), 3)]
        elif chunk_type == b"tRNS":
            transparency = list(chunk_data)
        elif chunk_type == b"IDAT":
            compressed.extend(chunk_data)
        elif chunk_type == b"IEND":
            break

    if bit_depth != 8:
        raise ValueError(f"Unsupported PNG bit depth: {bit_depth}")

    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}.get(color_type)
    if channels is None:
        raise ValueError(f"Unsupported PNG color type: {color_type}")

    raw = zlib.decompress(bytes(compressed))
    stride = width * channels
    rows: list[bytes] = []
    offset = 0
    previous = [0] * stride

    for _ in range(height):
        filter_type = raw[offset]
        offset += 1
        scanline = list(raw[offset:offset + stride])
        offset += stride

        for i, value in enumerate(scanline):
            left = scanline[i - channels] if i >= channels else 0
            up = previous[i]
            up_left = previous[i - channels] if i >= channels else 0

            if filter_type == 1:
                scanline[i] = (value + left) & 0xFF
            elif filter_type == 2:
                scanline[i] = (value + up) & 0xFF
            elif filter_type == 3:
                scanline[i] = (value + ((left + up) // 2)) & 0xFF
            elif filter_type == 4:
                p = left + up - up_left
                pa, pb, pc = abs(p - left), abs(p - up), abs(p - up_left)
                predictor = left if pa <= pb and pa <= pc else up if pb <= pc else up_left
                scanline[i] = (value + predictor) & 0xFF
            elif filter_type != 0:
                raise ValueError(f"Unsupported PNG filter: {filter_type}")

        previous = scanline
        rows.append(bytes(scanline))

    pixels: list[tuple[int, int, int, int]] = []
    for row in rows:
        for x in range(width):
            i = x * channels
            if color_type == 6:
                pixels.append((row[i], row[i + 1], row[i + 2], row[i + 3]))
            elif color_type == 2:
                pixels.append((row[i], row[i + 1], row[i + 2], 255))
            elif color_type == 3:
                r, g, b = palette[row[i]]
                a = transparency[row[i]] if row[i] < len(transparency) else 255
                pixels.append((r, g, b, a))
            elif color_type == 4:
                pixels.append((row[i], row[i], row[i], row[i + 1]))
            else:
                pixels.append((row[i], row[i], row[i], 255))

    return width, height, pixels


def write_png(path: Path, width: int, height: int, pixels: list[tuple[int, int, int, int]]) -> None:
    def chunk(name: bytes, payload: bytes) -> bytes:
        return struct.pack(">I", len(payload)) + name + payload + struct.pack(">I", binascii.crc32(name + payload) & 0xFFFFFFFF)

    raw = bytearray()
    for y in range(height):
        raw.append(0)
        for pixel in pixels[y * width:(y + 1) * width]:
            raw.extend(pixel)

    payload = struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0)
    png = b"\x89PNG\r\n\x1a\n" + chunk(b"IHDR", payload) + chunk(b"IDAT", zlib.compress(bytes(raw), 9)) + chunk(b"IEND", b"")
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(png)


def local_asset_candidates(source: TextureSource) -> list[Path]:
    asset = Path(source.asset_path)
    return [
        ROOT / "src/main/resources" / asset,
        ROOT / "src/generated/resources" / asset,
        ROOT / "run/client/kubejs" / asset,
        ROOT / "run/client/kubejs/assets" / source.namespace / "textures" / f"{source.path}.png",
    ]


def read_asset_from_disk(source: TextureSource) -> bytes | None:
    for candidate in local_asset_candidates(source):
        if candidate.exists():
            return candidate.read_bytes()

    mods_dir = ROOT / "mods"
    if mods_dir.exists():
        suffix = Path(source.asset_path)
        for candidate in mods_dir.rglob(suffix.name):
            if candidate.as_posix().endswith(suffix.as_posix()):
                return candidate.read_bytes()
    return None


def read_asset_from_jars(source: TextureSource) -> bytes | None:
    search_roots = [ROOT / ".gradle", Path.home() / ".gradle/caches/modules-2/files-2.1"]
    interesting = (source.namespace, source.namespace.replace("_", "-"), source.namespace.replace("_", ""))

    for root in search_roots:
        if not root.exists():
            continue
        for dirpath, _, filenames in os.walk(root):
            lower_dir = dirpath.lower()
            if not any(token in lower_dir for token in interesting):
                continue
            for filename in filenames:
                if not filename.endswith(".jar"):
                    continue
                jar_path = Path(dirpath) / filename
                try:
                    with zipfile.ZipFile(jar_path) as jar:
                        try:
                            return jar.read(source.asset_path)
                        except KeyError:
                            continue
                except zipfile.BadZipFile:
                    continue
    return None


def source_color(source: TextureSource) -> tuple[int, int, int]:
    data = read_asset_from_disk(source) or read_asset_from_jars(source)
    if data is None:
        return FALLBACK_COLORS[source.wood]

    try:
        _, _, pixels = read_png(data)
    except ValueError:
        return FALLBACK_COLORS[source.wood]

    visible = [(r, g, b) for r, g, b, a in pixels if a > 16]
    if not visible:
        return FALLBACK_COLORS[source.wood]
    return tuple(sum(pixel[i] for pixel in visible) // len(visible) for i in range(3))


def shade(color: tuple[int, int, int], factor: float) -> tuple[int, int, int, int]:
    return tuple(max(0, min(255, round(channel * factor))) for channel in color) + (255,)


def draw_rect(pixels: list[tuple[int, int, int, int]], x0: int, y0: int, x1: int, y1: int, color: tuple[int, int, int, int]) -> None:
    for y in range(y0, y1):
        for x in range(x0, x1):
            pixels[y * 16 + x] = color


def set_pixel(pixels: list[tuple[int, int, int, int]], x: int, y: int, color: tuple[int, int, int, int]) -> None:
    if 0 <= x < 16 and 0 <= y < 16:
        pixels[y * 16 + x] = color


def generate_texture(base: tuple[int, int, int]) -> list[tuple[int, int, int, int]]:
    transparent = (0, 0, 0, 0)
    pixels = [transparent for _ in range(16 * 16)]
    wood_highlight = shade(base, 1.35)
    wood_light = shade(base, 1.12)
    wood_mid = shade(base, 0.90)
    wood_dark = shade(base, 0.58)

    # model samples the plank/rung faces from y=1..7.
    draw_rect(pixels, 0, 1, 16, 7, wood_mid)
    draw_rect(pixels, 0, 1, 16, 2, wood_highlight)
    draw_rect(pixels, 0, 2, 16, 3, wood_light)
    draw_rect(pixels, 0, 4, 16, 5, wood_dark)
    draw_rect(pixels, 0, 5, 16, 6, wood_mid)
    draw_rect(pixels, 0, 6, 16, 7, wood_dark)

    # Small plank-grain accents, subtle enough that variants stay readable at 16x16.
    for x in range(0, 16, 4):
        set_pixel(pixels, x, 2, wood_dark)
        set_pixel(pixels, x + 2, 1, wood_light)
        set_pixel(pixels, x + 1, 5, wood_highlight)

    # ropes and knots sample from y=9..15.
    draw_rect(pixels, 0, 9, 16, 16, ROPE_BASE)
    for y in range(9, 16):
        for x in range(16):
            if (x + y) % 4 == 0:
                set_pixel(pixels, x, y, ROPE_LIGHT)
            elif (x * 2 + y) % 5 == 0:
                set_pixel(pixels, x, y, ROPE_DARK)

    # Four knot patches correspond to knot UVR and make the side ropes look tied on.
    for x0, y0 in ((2, 10), (5, 12), (9, 11), (12, 12)):
        draw_rect(pixels, x0, y0, x0 + 3, y0 + 3, ROPE_DARK)
        set_pixel(pixels, x0, y0, ROPE_LIGHT)
        set_pixel(pixels, x0 + 1, y0 + 1, ROPE_BASE)
        set_pixel(pixels, x0 + 2, y0 + 2, ROPE_LIGHT)

    return pixels


def main() -> None:
    OUT_DIR.mkdir(parents=True, exist_ok=True)
    for source in texture_sources():
        color = source_color(source)
        pixels = generate_texture(color)
        write_png(OUT_DIR / f"{source.wood}.png", 16, 16, pixels)
        print(f"generated {source.wood} from {source.namespace}:{source.path} using {color}")


if __name__ == "__main__":
    main()
