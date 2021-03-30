import time
from threading import Thread


class FetcherThread(Thread):
    """
    Every 5 minutes, refreshes the cards results.
    """
    def __init__(self, cards_db):
        self.cards_db = cards_db

    def run(self):
        global cards

        while True:
            print("Waiting for 10 minutes.")
            time.sleep(60 * 10)
            print("Fetching cards again.")
            self.cards_db.refresh()
            cards = self.cards_db.collection.get_rows()
            print("Finished fetching cards.")

