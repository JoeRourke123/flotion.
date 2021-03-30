from notion.collection import CollectionRowBlock, Collection

from utils.consts import COVER_MAPPINGS, BLOCK_VALUE_MAPPING, BLOCK_TYPE_MAPPING


def get_card_title(item: CollectionRowBlock):
    return item.title_plaintext


def get_card_content(item: CollectionRowBlock):
    contents = []
    list_block = []
    is_list = False

    for child in item.children:
        val = getattr(child, BLOCK_VALUE_MAPPING[str(child.type)])

        is_list = str(child.type) == "bulleted_list"
        if is_list:
            list_block.append(val)
            continue
        elif len(list_block) > 0:
            contents.append({
                "type": "list",
                "value": list_block
            })
            list_block = []

        if val != "":
            contents.append({
                "type": BLOCK_TYPE_MAPPING[str(child.type)],
                "value": val
            })

    if len(list_block) > 0:
        contents.append({
            "type": "list",
            "value": list_block
        })

    return contents


def get_card_module(item: CollectionRowBlock):
    return item.module


def get_cover_level(item: CollectionRowBlock):
    return COVER_MAPPINGS.get(item.cover, "red")


def get_modules_from_collection(coll: Collection):
    properties = coll.get_schema_properties()
    module_list = []

    for property in properties:
        if property["slug"] == "module":
            module_list = list(map(lambda x: x["value"], property["options"]))

    return sorted(module_list)


def get_item_from_collection(col: list, id: str) -> CollectionRowBlock:
    for item in col:
        if str(item.id) == id:
            return item
    return None
