from datetime import datetime
from os import environ
from urllib.parse import quote, unquote
from flask import Flask, render_template
from notion.client import NotionClient
from notion.collection import Collection, CollectionRowBlock
from random import choice

app = Flask(__name__)
notion = NotionClient(
    token_v2=environ.get("FLOTION_TOKEN", None),
    enable_caching=True,
)

GREEN_LIMIT = 12
YELLOW_LIMIT = 6
CARD_PAGE = "https://www.notion.so/joerourke/cc94ab7b40e5445dbabf5cddf6e6863a?v=04f75288e34242798be38589207a1486"
BLOCK_TYPE_MAPPING = {
    "image": "img",
    "text": "text"
}
BLOCK_VALUE_MAPPING = {
    "image": "source",
    "text": "title",
    "bulleted_list": "title",
}
COVER_MAPPINGS = {
    "/images/page-cover/solid_red.png": "red",
    "/images/page-cover/solid_yellow.png": "yellow",
    "/images/page-cover/solid_blue.png": "green"
}


@app.route('/')
def main():
    return render_template('index.html')


@app.route('/questions')
def questions():
    return render_template('quiz.html')


@app.route('/stats')
def stats_view():
    return render_template('statistics.html')


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

def __get_modules_from_collection(coll: Collection):
    properties = coll.get_schema_properties()
    module_list = []

    for property in properties:
        if property["slug"] == "module":
            module_list = list(map(lambda x: x["value"], property["options"]))

    return sorted(module_list)

def __get_item_from_collection(col: list, id: str) -> CollectionRowBlock:
    for item in col:
        if str(item.id) == id:
            return item
    return None


def __change_cover(item: CollectionRowBlock, correct=None):
    if correct is None:
        corrects = item.correct
    else:
        corrects = correct

    cover = item.cover

    limits = [GREEN_LIMIT, YELLOW_LIMIT, 0]
    covers = ["/images/page-cover/solid_blue.png",
              "/images/page-cover/solid_yellow.png",
              "/images/page-cover/solid_red.png"]
    for i in range(len(limits)):
        if corrects >= limits[i] and cover != covers[i]:
            item.cover = covers[i]
            break


@app.route("/c/<path:url>", methods=["POST"])
def correct(url):
    try:
        item = notion.get_block(unquote(url))
    except Exception as e:
        print(e)
        return {"error": True}

    item.answered = datetime.now()
    got_correct = item.correct
    item.correct = got_correct + 1

    __change_cover(item, correct=got_correct + 1)

    return {"success": True}


@app.route("/w/<path:url>", methods=["POST"])
def incorrect(url):
    try:
        item = notion.get_block(unquote(url))
    except:
        return {"error": True}

    got_correct = item.correct
    item.correct = got_correct - 1

    __change_cover(item, correct=got_correct - 1)

    return {"success": True}


@app.route('/modules')
def modules():
    db = notion.get_collection_view(CARD_PAGE)
    coll: Collection = db.collection

    return {"modules": __get_modules_from_collection(coll)}


@app.route('/q/<module>')
def question(module: str):
    db = notion.get_collection_view(CARD_PAGE)
    coll: Collection = db.collection

    filtered = coll.get_rows()

    show_level = "all"
    for level in ["red", "yellow", "green"]:
        if module.endswith(f"__{level}__"):
            show_level = level
            break

    if module != "all":
        filtered = list(filter(lambda x: get_card_module(x) == module and
                                        True if show_level == "all" else get_cover_level(x) == show_level, filtered))
    elif show_level != "all":
        filtered = list(filter(lambda x: get_cover_level(x) == show_level, filtered))

    populated = False
    parsed_card = {"error": True}

    while not populated and len(filtered) > 0:
        random_item: CollectionRowBlock = choice(filtered,)
        parsed_card = {
            "id": quote(random_item.get_browseable_url()),
            "title": get_card_title(random_item),
            "content": get_card_content(random_item),
            "module": get_card_module(random_item),
            "level": get_cover_level(random_item)
        }
        populated = len(parsed_card["content"]) > 0

    return parsed_card


@app.route("/s")
def stats_data():
    db = notion.get_collection_view(CARD_PAGE)
    coll: Collection = db.collection

    rows = coll.get_rows()

    modules = __get_modules_from_collection(coll)
    module_mapping = {m: [0, 0, 0] for m in modules}
    module_cards = {m: 0 for m in modules}

    response = {
        "total_red": 0,
        "total_yellow": 0,
        "total_green": 0,
        "total_corrects": 0,
        "total_cards": 0,
    }

    for item in rows:
        cover = get_cover_level(item)
        response[f"total_{ cover }"] += 1
        response["total_corrects"] += 0 if item.correct is None else item.correct
        response["total_cards"] += 1

        cover_index = ["red", "green", "yellow"].index(cover)
        if item.module is not None:
            module_mapping[item.module][cover_index] += 1
            module_cards[item.module] += 1

    for module in modules:
        card_count = module_cards[module]

        if card_count == 0:
            card_count = 1

        module_mapping[module][0] /= card_count
        module_mapping[module][1] /= card_count
        module_mapping[module][2] /= card_count

    response["ranked_modules"] = sorted(modules, key=lambda x: module_mapping[x])

    return response


if __name__ == '__main__':
    app.run(host='0.0.0.0')
