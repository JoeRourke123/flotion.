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

CARD_PAGE = "https://www.notion.so/joerourke/cc94ab7b40e5445dbabf5cddf6e6863a?v=04f75288e34242798be38589207a1486"
BLOCK_TYPE_MAPPING = {
    "image": "img",
    "text": "text"
}
BLOCK_VALUE_MAPPING = {
    "image": "source",
    "text": "title",
}


@app.route('/')
def main():
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
    return {
        "/images/page-cover/solid_red.png": "red",
        "/images/page-cover/solid_yellow.png": "yellow",
        "/images/page-cover/solid_blue.png": "green"
    }.get(item.cover, "red")


@app.route('/q/<module>')
def question(module):
    db = notion.get_collection_view(CARD_PAGE)
    coll: Collection = db.collection

    filtered = coll.get_rows()
    if module != "all":
        filtered = list(filter(lambda x: get_card_module(x) == module, filtered))

    populated = False
    parsed_card = {"error": True}

    while not populated and len(filtered) > 0:
        random_item = choice(filtered)

        parsed_card = {
            "title": get_card_title(random_item),
            "content": get_card_content(random_item),
            "module": get_card_module(random_item),
            "level": get_cover_level(random_item)
        }
        populated = len(parsed_card["content"]) > 0

    return parsed_card


if __name__ == '__main__':
    app.run()
