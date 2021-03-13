from datetime import datetime
from urllib.parse import quote, unquote
from flask import Flask, render_template
from notion.block import Block
from notion.client import NotionClient
from notion.collection import Collection, CollectionRowBlock
from random import choice

app = Flask(__name__)
notion = NotionClient(
    token_v2="3daa339104103a00940a3678dc4a54be431803a5f7bd48efe84064d47f113685656a1a4abac54868dbfd2bb1b302"
             "b6386f38052d369ab580a0f27506f45fa5c22a294546f5f879da641e923247e2"
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


def get_card_title(item: CollectionRowBlock):
    return item.title_plaintext


def get_card_content(item: CollectionRowBlock):
    print(item.children)
    contents = []

    for child in item.children:
        val = getattr(child, BLOCK_VALUE_MAPPING[str(child.type)])

        if val != "":
            contents.append({
                "type": BLOCK_TYPE_MAPPING[str(child.type)],
                "value": val
            })

    print(contents)
    return contents


def get_card_module(item: CollectionRowBlock):
    module = item.get_property("Tags")
    return None if len(module) < 1 else module[0]


def get_cover_level(item: CollectionRowBlock):
    return COVER_MAPPINGS.get(item.cover, "red")


def __get_item_from_collection(col: list, id: str) -> CollectionRowBlock:
    for item in col:
        if str(item.id) == id:
            return item
    return None


def __change_cover(item: CollectionRowBlock, correct=None):
    if correct is None:
        corrects = item.get_property("Correct")
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

    item.set_property("Last Got Correct", datetime.now())
    got_correct = item.get_property("Correct")
    item.set_property("Correct", got_correct + 1)

    __change_cover(item, correct=got_correct + 1)

    return {"success": True}


@app.route("/w/<path:url>", methods=["POST"])
def incorrect(url):
    try:
        item = notion.get_block(unquote(url))
    except:
        return {"error": True}

    got_correct = item.get_property("Correct")
    item.set_property("Correct", got_correct - 1)

    __change_cover(item, correct=got_correct - 1)

    return {"success": True}


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
    else:
        filtered = list(filter(lambda x: True if show_level == "all" else get_cover_level(x) == show_level, filtered))

    populated = False
    parsed_card = {"error": True}

    while not populated and len(filtered) > 0:
        random_item: CollectionRowBlock = choice(filtered)
        parsed_card = {
            "id": quote(random_item.get_browseable_url()),
            "title": get_card_title(random_item),
            "content": get_card_content(random_item),
            "module": get_card_module(random_item),
            "level": get_cover_level(random_item)
        }
        populated = len(parsed_card["content"]) > 0

    return parsed_card


if __name__ == '__main__':
    app.run()
