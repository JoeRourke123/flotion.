import random

from notion.block import Block
from notion.client import NotionClient
from notion.collection import CollectionRowBlock, Collection, CollectionView

from utils.consts import COVER_MAPPINGS, BLOCK_VALUE_MAPPING, BLOCK_TYPE_MAPPING, MAX_CARD_LIMIT


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


def pick_random_card(self: NotionClient, coll_view: CollectionView, query=None) -> CollectionRowBlock:
    if query is None:
        query = {
            "aggregations": [{"aggregator": "count"}]
        }

    results = self.post("queryCollection", data={
        "collectionId": coll_view.collection.id,
        "collectionViewId": coll_view.id,
        "query": query,
        "loader": {
            "type": "table",
            "limit": MAX_CARD_LIMIT,
            "searchQuery": "",
            "userTimeZone": "Europe/London",
            "loadContentCover": True
        }
    })

    parsed = results.json()["result"]
    block_ids = parsed["blockIds"]

    if parsed["total"] == 0:
        return None

    random_item = random.choice(block_ids)

    print(random_item)

    return self.get_block(random_item)


def count_results(self: NotionClient, coll_view: CollectionView, query=None) -> CollectionRowBlock:
    if query is None:
        query = {
            "aggregations": [{"aggregator": "count"}]
        }

    results = self.post("queryCollection", data={
        "collectionId": coll_view.collection.id,
        "collectionViewId": coll_view.id,
        "query": query,
        "loader": {
            "type": "table",
            "limit": MAX_CARD_LIMIT,
            "searchQuery": "",
            "userTimeZone": "Europe/London",
            "loadContentCover": True
        }
    })

    parsed = results.json()["result"]

    return parsed["total"]


def get_module_property_id(collection: Collection):
    for prop in collection.get_schema_properties():
        if prop["slug"] == "module":
            return prop["id"]


def get_correct_property_id(collection: Collection):
    for prop in collection.get_schema_properties():
        if prop["slug"] == "correct":
            return prop["id"]