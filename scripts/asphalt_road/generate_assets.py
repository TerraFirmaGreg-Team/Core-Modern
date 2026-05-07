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
        if name in {'block.json', 'slab.json',
                    'block_overlay_horizontal.json', 'block_overlay_vertical.json',
                    'slab_overlay_horizontal.json', 'slab_overlay_vertical.json'}:
            path.unlink(missing_ok=True)

    # Base asphalt models (untinted)
    dump(MODELS_BLOCK / 'block_base.json', {
        'parent': 'rnr:block/path_block',
        'textures': {'top': tex('block'), 'gravel': 'minecraft:block/gravel'}
    })
    dump(MODELS_BLOCK / 'slab_base.json', {
        'parent': 'rnr:block/path_slab',
        'textures': {'top': tex('block'), 'gravel': 'minecraft:block/gravel'}
    })

    # Overlay models: lower/upper to represent draw order at intersections.
    dump(MODELS_BLOCK / 'block_overlay_horizontal_lower.json', {
        'parent': 'block/block',
        'textures': {'line': tex('horizontal_white')},
        'elements': [{
            'from': [-0.01, 14.98, -0.01],
            'to': [16.01, 15.00, 16.01],
            'faces': {
                'up': {'uv': [0, 0, 16, 16], 'texture': '#line', 'tintindex': 1},
                'down': {'uv': [0, 0, 16, 16], 'texture': '#line', 'tintindex': 1}
            }
        }]
    })
    dump(MODELS_BLOCK / 'block_overlay_horizontal_upper.json', {
        'parent': 'block/block',
        'textures': {'line': tex('horizontal_white')},
        'elements': [{
            'from': [-0.01, 14.99, -0.01],
            'to': [16.01, 15.01, 16.01],
            'faces': {
                'up': {'uv': [0, 0, 16, 16], 'texture': '#line', 'tintindex': 1},
                'down': {'uv': [0, 0, 16, 16], 'texture': '#line', 'tintindex': 1}
            }
        }]
    })
    dump(MODELS_BLOCK / 'block_overlay_vertical_lower.json', {
        'parent': 'block/block',
        'textures': {'line': tex('vertical_white')},
        'elements': [{
            'from': [-0.01, 14.98, -0.01],
            'to': [16.01, 15.00, 16.01],
            'faces': {
                'up': {'uv': [0, 0, 16, 16], 'texture': '#line', 'tintindex': 2},
                'down': {'uv': [0, 0, 16, 16], 'texture': '#line', 'tintindex': 2}
            }
        }]
    })
    dump(MODELS_BLOCK / 'block_overlay_vertical_upper.json', {
        'parent': 'block/block',
        'textures': {'line': tex('vertical_white')},
        'elements': [{
            'from': [-0.01, 14.99, -0.01],
            'to': [16.01, 15.01, 16.01],
            'faces': {
                'up': {'uv': [0, 0, 16, 16], 'texture': '#line', 'tintindex': 2},
                'down': {'uv': [0, 0, 16, 16], 'texture': '#line', 'tintindex': 2}
            }
        }]
    })
    dump(MODELS_BLOCK / 'slab_overlay_horizontal_lower.json', {
        'parent': 'block/block',
        'textures': {'line': tex('horizontal_white')},
        'elements': [{
            'from': [-0.01, 6.98, -0.01],
            'to': [16.01, 7.00, 16.01],
            'faces': {
                'up': {'uv': [0, 0, 16, 16], 'texture': '#line', 'tintindex': 1},
                'down': {'uv': [0, 0, 16, 16], 'texture': '#line', 'tintindex': 1}
            }
        }]
    })
    dump(MODELS_BLOCK / 'slab_overlay_horizontal_upper.json', {
        'parent': 'block/block',
        'textures': {'line': tex('horizontal_white')},
        'elements': [{
            'from': [-0.01, 6.99, -0.01],
            'to': [16.01, 7.01, 16.01],
            'faces': {
                'up': {'uv': [0, 0, 16, 16], 'texture': '#line', 'tintindex': 1},
                'down': {'uv': [0, 0, 16, 16], 'texture': '#line', 'tintindex': 1}
            }
        }]
    })
    dump(MODELS_BLOCK / 'slab_overlay_vertical_lower.json', {
        'parent': 'block/block',
        'textures': {'line': tex('vertical_white')},
        'elements': [{
            'from': [-0.01, 6.98, -0.01],
            'to': [16.01, 7.00, 16.01],
            'faces': {
                'up': {'uv': [0, 0, 16, 16], 'texture': '#line', 'tintindex': 2},
                'down': {'uv': [0, 0, 16, 16], 'texture': '#line', 'tintindex': 2}
            }
        }]
    })
    dump(MODELS_BLOCK / 'slab_overlay_vertical_upper.json', {
        'parent': 'block/block',
        'textures': {'line': tex('vertical_white')},
        'elements': [{
            'from': [-0.01, 6.99, -0.01],
            'to': [16.01, 7.01, 16.01],
            'faces': {
                'up': {'uv': [0, 0, 16, 16], 'texture': '#line', 'tintindex': 2},
                'down': {'uv': [0, 0, 16, 16], 'texture': '#line', 'tintindex': 2}
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

    def stair_overlay_model(name: str, texture_name: str, tint_index: int, y_min: float, y_max: float):
        # Match RNR path stair geometry (lower 1/2 slab top at y=7 and upper steps at y=15).
        def top_patch(x1, z1, x2, z2):
            return {
                'from': [x1, y_min, z1],
                'to': [x2, y_max, z2],
                'faces': {
                    'up': {'uv': [x1, z1, x2, z2], 'texture': '#line', 'tintindex': tint_index},
                    'down': {'uv': [x1, z1, x2, z2], 'texture': '#line', 'tintindex': tint_index}
                }
            }

        variants = {
            'straight': [top_patch(-0.01, -0.01, 16.01, 7.01), top_patch(7.99, -0.01, 16.01, 16.01)],
            'outer': [top_patch(-0.01, -0.01, 16.01, 7.01), top_patch(7.99, 7.99, 16.01, 16.01)],
            'inner': [top_patch(-0.01, -0.01, 16.01, 7.01), top_patch(7.99, -0.01, 16.01, 16.01), top_patch(-0.01, 7.99, 8.01, 16.01)]
        }
        for shape, elements in variants.items():
            dump(MODELS_BLOCK / f'stairs_overlay_{shape}_{name}.json', {
                'parent': 'block/block',
                'textures': {'line': tex(texture_name)},
                'elements': elements
            })

    stair_overlay_model('horizontal_lower', 'horizontal_white', 1, 6.98, 7.00)
    stair_overlay_model('horizontal_upper', 'horizontal_white', 1, 6.99, 7.01)
    stair_overlay_model('vertical_lower', 'vertical_white', 2, 6.98, 7.00)
    stair_overlay_model('vertical_upper', 'vertical_white', 2, 6.99, 7.01)


def gen_blockstates():
    colors = "white|orange|magenta|light_blue|yellow|lime|pink|gray|light_gray|cyan|purple|blue|brown|green|red|black"
    dump(BLOCKSTATES / 'asphalt_road.json', {
        'multipart': [
            {'apply': {'model': model_path('block_base')}},
            {'when': {'horizontal_color': colors, 'vertical_color': 'none'}, 'apply': {'model': model_path('block_overlay_horizontal_upper')}},
            {'when': {'vertical_color': colors, 'horizontal_color': 'none'}, 'apply': {'model': model_path('block_overlay_vertical_upper')}},
            {'when': {'horizontal_color': colors, 'vertical_color': colors, 'top_layer': 'horizontal'}, 'apply': {'model': model_path('block_overlay_vertical_lower')}},
            {'when': {'horizontal_color': colors, 'vertical_color': colors, 'top_layer': 'horizontal'}, 'apply': {'model': model_path('block_overlay_horizontal_upper')}},
            {'when': {'horizontal_color': colors, 'vertical_color': colors, 'top_layer': 'vertical'}, 'apply': {'model': model_path('block_overlay_horizontal_lower')}},
            {'when': {'horizontal_color': colors, 'vertical_color': colors, 'top_layer': 'vertical'}, 'apply': {'model': model_path('block_overlay_vertical_upper')}}
        ]
    })
    dump(BLOCKSTATES / 'asphalt_road_slab.json', {
        'multipart': [
            {'apply': {'model': model_path('slab_base')}},
            {'when': {'horizontal_color': colors, 'vertical_color': 'none'}, 'apply': {'model': model_path('slab_overlay_horizontal_upper')}},
            {'when': {'vertical_color': colors, 'horizontal_color': 'none'}, 'apply': {'model': model_path('slab_overlay_vertical_upper')}},
            {'when': {'horizontal_color': colors, 'vertical_color': colors, 'top_layer': 'horizontal'}, 'apply': {'model': model_path('slab_overlay_vertical_lower')}},
            {'when': {'horizontal_color': colors, 'vertical_color': colors, 'top_layer': 'horizontal'}, 'apply': {'model': model_path('slab_overlay_horizontal_upper')}},
            {'when': {'horizontal_color': colors, 'vertical_color': colors, 'top_layer': 'vertical'}, 'apply': {'model': model_path('slab_overlay_horizontal_lower')}},
            {'when': {'horizontal_color': colors, 'vertical_color': colors, 'top_layer': 'vertical'}, 'apply': {'model': model_path('slab_overlay_vertical_upper')}}
        ]
    })

    stair_base = []
    stair_variant_refs = []

    def base_rule(facing, shape, model, overlay_shape, y=None):
        when = {'facing': facing, 'shape': shape}
        apply = {'model': model}
        if y is not None:
            apply['y'] = y
            apply['uvlock'] = True
        stair_base.append({'when': when, 'apply': apply})
        stair_variant_refs.append((when, y, overlay_shape))

    base_rule('east', 'straight', model_path('stairs'), 'straight')
    base_rule('west', 'straight', model_path('stairs'), 'straight', 180)
    base_rule('south', 'straight', model_path('stairs'), 'straight', 90)
    base_rule('north', 'straight', model_path('stairs'), 'straight', 270)
    base_rule('east', 'outer_right', model_path('stairs_outer'), 'outer')
    base_rule('west', 'outer_right', model_path('stairs_outer'), 'outer', 180)
    base_rule('south', 'outer_right', model_path('stairs_outer'), 'outer', 90)
    base_rule('north', 'outer_right', model_path('stairs_outer'), 'outer', 270)
    base_rule('east', 'outer_left', model_path('stairs_outer'), 'outer', 270)
    base_rule('west', 'outer_left', model_path('stairs_outer'), 'outer', 90)
    base_rule('south', 'outer_left', model_path('stairs_outer'), 'outer')
    base_rule('north', 'outer_left', model_path('stairs_outer'), 'outer', 180)
    base_rule('east', 'inner_right', model_path('stairs_inner'), 'inner')
    base_rule('west', 'inner_right', model_path('stairs_inner'), 'inner', 180)
    base_rule('south', 'inner_right', model_path('stairs_inner'), 'inner', 90)
    base_rule('north', 'inner_right', model_path('stairs_inner'), 'inner', 270)
    base_rule('east', 'inner_left', model_path('stairs_inner'), 'inner', 270)
    base_rule('west', 'inner_left', model_path('stairs_inner'), 'inner', 90)
    base_rule('south', 'inner_left', model_path('stairs_inner'), 'inner')
    base_rule('north', 'inner_left', model_path('stairs_inner'), 'inner', 180)

    def add_stair_overlay(entries: list, color_when: dict, suffix: str):
        for base_when, y, overlay_shape in stair_variant_refs:
            apply = {'model': model_path(f'stairs_overlay_{overlay_shape}_{suffix}')}
            if y is not None:
                apply['y'] = y
                apply['uvlock'] = True
            entries.append({'when': {**base_when, **color_when}, 'apply': apply})

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
