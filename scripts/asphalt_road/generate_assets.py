#!/usr/bin/env python3
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src/main/resources/assets/tfg"
MODELS_BLOCK = ASSETS / "models/block/asphalt_road"
MODELS_ITEM = ASSETS / "models/item"
BLOCKSTATES = ASSETS / "blockstates"

def dump(path: Path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n")


def model_path(name: str) -> str:
    return f"tfg:block/asphalt_road/{name}"


def tex(name: str) -> str:
    return f"tfg:block/asphalt_road/{name}"


def gen_models():
    for path in MODELS_BLOCK.glob('*.json'):
        name = path.name
        if name.startswith(('block_horizontal_', 'block_vertical_', 'block_cross_',
                            'slab_horizontal_', 'slab_vertical_', 'slab_cross_', 'stairs_')) and name not in {
            'stairs.json', 'stairs_outer.json', 'stairs_inner.json'
        }:
            path.unlink(missing_ok=True)
        if name in {'block.json', 'slab.json'}:
            path.unlink(missing_ok=True)
        if 'overlay' in name and name.endswith('_lower.json'):
            path.unlink(missing_ok=True)

    for legacy in ('block_overlay_horizontal_upper.json', 'block_overlay_vertical_upper.json',
                   'slab_overlay_horizontal_upper.json', 'slab_overlay_vertical_upper.json'):
        (MODELS_BLOCK / legacy).unlink(missing_ok=True)

    # Base asphalt models (untinted)
    dump(MODELS_BLOCK / 'block_base.json', {
        'parent': 'rnr:block/path_block',
        'textures': {'top': tex('block'), 'gravel': 'minecraft:block/gravel'}
    })
    dump(MODELS_BLOCK / 'slab_base.json', {
        'parent': 'rnr:block/path_slab',
        'textures': {'top': tex('block'), 'gravel': 'minecraft:block/gravel'}
    })

    # One decal layer; block state uses `decal` + `color` (see AsphaltRoadDecal / AsphaltRoadMarkingColor in Java).
    dump(MODELS_BLOCK / 'block_overlay_horizontal.json', {
        'parent': 'block/block',
        'textures': {'decal': tex('mask_line_horizontal')},
        'elements': [{
            'from': [-0.01, 14.992, -0.01],
            'to': [16.01, 15.015, 16.01],
            'faces': {
                'up': {'uv': [0, 0, 16, 16], 'texture': '#decal', 'tintindex': 1},
                'down': {'uv': [0, 0, 16, 16], 'texture': '#decal', 'tintindex': 1}
            }
        }]
    })
    dump(MODELS_BLOCK / 'block_overlay_vertical.json', {
        'parent': 'block/block',
        'textures': {'decal': tex('mask_line_vertical')},
        'elements': [{
            'from': [-0.01, 14.992, -0.01],
            'to': [16.01, 15.015, 16.01],
            'faces': {
                'up': {'uv': [0, 0, 16, 16], 'texture': '#decal', 'tintindex': 2},
                'down': {'uv': [0, 0, 16, 16], 'texture': '#decal', 'tintindex': 2}
            }
        }]
    })
    dump(MODELS_BLOCK / 'slab_overlay_horizontal.json', {
        'parent': 'block/block',
        'textures': {'decal': tex('mask_line_horizontal')},
        'elements': [{
            'from': [-0.01, 6.978, -0.01],
            'to': [16.01, 7.005, 16.01],
            'faces': {
                'up': {'uv': [0, 0, 16, 16], 'texture': '#decal', 'tintindex': 1},
                'down': {'uv': [0, 0, 16, 16], 'texture': '#decal', 'tintindex': 1}
            }
        }]
    })
    dump(MODELS_BLOCK / 'slab_overlay_vertical.json', {
        'parent': 'block/block',
        'textures': {'decal': tex('mask_line_vertical')},
        'elements': [{
            'from': [-0.01, 6.978, -0.01],
            'to': [16.01, 7.005, 16.01],
            'faces': {
                'up': {'uv': [0, 0, 16, 16], 'texture': '#decal', 'tintindex': 2},
                'down': {'uv': [0, 0, 16, 16], 'texture': '#decal', 'tintindex': 2}
            }
        }]
    })
    dump(MODELS_BLOCK / 'block_overlay_cross.json', {
        'parent': 'block/block',
        'textures': {'decal': tex('mask_cross')},
        'elements': [{
            'from': [-0.01, 14.992, -0.01],
            'to': [16.01, 15.015, 16.01],
            'faces': {
                'up': {'uv': [0, 0, 16, 16], 'texture': '#decal', 'tintindex': 3},
                'down': {'uv': [0, 0, 16, 16], 'texture': '#decal', 'tintindex': 3}
            }
        }]
    })
    dump(MODELS_BLOCK / 'slab_overlay_cross.json', {
        'parent': 'block/block',
        'textures': {'decal': tex('mask_cross')},
        'elements': [{
            'from': [-0.01, 6.978, -0.01],
            'to': [16.01, 7.005, 16.01],
            'faces': {
                'up': {'uv': [0, 0, 16, 16], 'texture': '#decal', 'tintindex': 3},
                'down': {'uv': [0, 0, 16, 16], 'texture': '#decal', 'tintindex': 3}
            }
        }]
    })

    dump(MODELS_BLOCK / 'stairs.json', {
        'parent': 'rnr:block/path_stairs',
        'textures': {'bottom': tex('block'), 'top': tex('block'), 'side': tex('block')}
    })
    dump(MODELS_BLOCK / 'stairs_outer.json', {
        'parent': 'rnr:block/path_outer_stairs',
        'textures': {'bottom': tex('block'), 'top': tex('block'), 'side': tex('block')}
    })
    dump(MODELS_BLOCK / 'stairs_inner.json', {
        'parent': 'rnr:block/path_inner_stairs',
        'textures': {'bottom': tex('block'), 'top': tex('block'), 'side': tex('block')}
    })


def gen_blockstates():
    colors = "white|orange|magenta|light_blue|yellow|lime|pink|gray|light_gray|cyan|purple|blue|brown|green|red|black"
    dump(BLOCKSTATES / 'asphalt_road.json', {
        'multipart': [
            {'apply': {'model': model_path('block_base')}},
            {'when': {'decal': 'line_horizontal', 'color': colors}, 'apply': {'model': model_path('block_overlay_horizontal')}},
            {'when': {'decal': 'line_vertical', 'color': colors}, 'apply': {'model': model_path('block_overlay_vertical')}},
            {'when': {'decal': 'cross', 'color': colors}, 'apply': {'model': model_path('block_overlay_cross')}},
        ]
    })
    dump(BLOCKSTATES / 'asphalt_road_slab.json', {
        'multipart': [
            {'apply': {'model': model_path('slab_base')}},
            {'when': {'decal': 'line_horizontal', 'color': colors}, 'apply': {'model': model_path('slab_overlay_horizontal')}},
            {'when': {'decal': 'line_vertical', 'color': colors}, 'apply': {'model': model_path('slab_overlay_vertical')}},
            {'when': {'decal': 'cross', 'color': colors}, 'apply': {'model': model_path('slab_overlay_cross')}},
        ]
    })

    stair_base = []

    def base_rule(facing, shape, model, y=None):
        when = {'facing': facing, 'shape': shape}
        apply = {'model': model}
        if y is not None:
            apply['y'] = y
            apply['uvlock'] = True
        stair_base.append({'when': when, 'apply': apply})

    base_rule('east', 'straight', model_path('stairs'))
    base_rule('west', 'straight', model_path('stairs'), 180)
    base_rule('south', 'straight', model_path('stairs'), 90)
    base_rule('north', 'straight', model_path('stairs'), 270)
    base_rule('east', 'outer_right', model_path('stairs_outer'))
    base_rule('west', 'outer_right', model_path('stairs_outer'), 180)
    base_rule('south', 'outer_right', model_path('stairs_outer'), 90)
    base_rule('north', 'outer_right', model_path('stairs_outer'), 270)
    base_rule('east', 'outer_left', model_path('stairs_outer'), 270)
    base_rule('west', 'outer_left', model_path('stairs_outer'), 90)
    base_rule('south', 'outer_left', model_path('stairs_outer'))
    base_rule('north', 'outer_left', model_path('stairs_outer'), 180)
    base_rule('east', 'inner_right', model_path('stairs_inner'))
    base_rule('west', 'inner_right', model_path('stairs_inner'), 180)
    base_rule('south', 'inner_right', model_path('stairs_inner'), 90)
    base_rule('north', 'inner_right', model_path('stairs_inner'), 270)
    base_rule('east', 'inner_left', model_path('stairs_inner'), 270)
    base_rule('west', 'inner_left', model_path('stairs_inner'), 90)
    base_rule('south', 'inner_left', model_path('stairs_inner'))
    base_rule('north', 'inner_left', model_path('stairs_inner'), 180)

    # Stairs use plain asphalt surface only (no painted line overlays).
    dump(BLOCKSTATES / 'asphalt_road_stairs.json', {'multipart': stair_base})

def gen_item_models():
    dump(MODELS_ITEM / 'asphalt_road.json', {'parent': model_path('block_base')})
    dump(MODELS_ITEM / 'asphalt_road_slab.json', {'parent': model_path('slab_base')})
    dump(MODELS_ITEM / 'asphalt_road_stairs.json', {'parent': model_path('stairs')})


def main():
    gen_models()
    gen_blockstates()
    gen_item_models()
    print('generated asphalt_road assets')


if __name__ == '__main__':
    main()
