import time
from datetime import datetime
from os import environ
from urllib.parse import quote, unquote
from flask import Flask, render_template
from notion.client import NotionClient
from notion.collection import CollectionView, Collection, CollectionRowBlock
from random import choice

from utils.consts import CARD_PAGE, BLOCK_ID
from utils.exporter import export_to_html
from utils.fetcher_thread import FetcherThread
from utils.filter import filter_cards
from utils.modify_cards import change_cover
from utils.card_details import get_cover_level, get_card_module, get_card_content, get_card_title, \
    get_modules_from_collection
from utils.stats import get_stats


app = Flask(__name__)
app.secret_key = environ.get("FLOTION_TOKEN", None)
notion = NotionClient(
    token_v2=environ.get("FLOTION_TOKEN", None),
    enable_caching=True
)

cards_db: CollectionView = notion.get_collection_view(CARD_PAGE)
cards = cards_db.collection.get_rows()

# fetcher = FetcherThread()
# fetcher.start()


@app.route('/')
def main():
    return render_template('index.html')


@app.route('/questions')
def questions():
    return render_template('quiz.html')


@app.route('/stats')
def stats_view():
    return render_template('statistics.html')


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

    change_cover(item, correct=got_correct + 1)

    return {"success": True}


@app.route('/modules')
def modules():
    db = notion.get_collection_view(CARD_PAGE)
    coll: Collection = db.collection

    return {"modules": get_modules_from_collection(coll)}


@app.route('/q/<card_filter>')
def question(card_filter: str):
    filtered = filter_cards(cards, card_filter)

    populated = False
    parsed_card = {"error": True}

    while not populated and len(filtered) > 0:
        random_item: CollectionRowBlock = choice(filtered, )
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
    rows = cards
    print(len(rows))

    modules = get_modules_from_collection(cards_db.collection)

    return get_stats(rows, modules)


if __name__ == '__main__':
    import sys
    args = sys.argv[1:]
    print(args)

    if len(args) > 0 and args[0] == "export":
        print("Exporting to HTML")
        export_to_html(notion, BLOCK_ID)
    else:
        app.run(host='0.0.0.0')
