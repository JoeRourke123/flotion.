import json
import time
import zipfile
import os

import requests
from notion.client import NotionClient


def export_to_html(client: NotionClient, table_id: str):
    if len(table_id) == 32:
        # need to convert id
        table_id = f'{table_id[0:8]}-{table_id[8:12]}-{table_id[12:16]}-{table_id[16:20]}-{table_id[20:]}'
        print('table id converted')
    elif len(table_id) != 36:
        raise Exception('Invalid table ID!')

    print('tableId', table_id)

    data = {
        "task": {
            "eventName": "exportBlock",
            "request": {
                "recursive": True,
                "blockId": table_id,
                "exportOptions": {
                    "exportType": "html",
                    "locale": "en",
                    "timeZone": "Europe/London",
                },
            }
        }
    }

    r = client.post('enqueueTask', data=data)
    print(r.json())

    exportTaskID = r.json()['taskId']
    print(f'task id is {exportTaskID}')

    state = 'in_progress'
    url = None
    waited = 0
    while True:
        time.sleep(10)
        waited += 1
        if waited >= 20:
            print('waited 200s, abort!')
            break

        r = client.post('getTasks', data={
            "taskIds": [exportTaskID]
        })
        results = r.json()['results'][0]
        print(r.json())
        state = results['state']

        if state == 'success':
            status = results['status']
            url = status['exportURL']
            break
        elif state != 'in_progress':
            print(r.json())
        else:
            print("Waiting")

    if url:
        try:
            os.remove("./flotion_export")
        except FileNotFoundError:
            pass

        with open('export.zip', 'wb') as f:
            r2 = requests.get(url, allow_redirects=True)
            f.write(r2.content)
        with zipfile.ZipFile('export.zip', 'r') as zip_ref:
            zip_ref.extractall('./flotion_export')
