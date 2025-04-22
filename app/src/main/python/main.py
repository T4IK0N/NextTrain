import requests
import json
import time
import random
import os
from bs4 import BeautifulSoup
from ParseTable import ParseTable
from Data import generate_url, save_html, user_agents
from ConfigManager import load_config, get_last_downloaded_filename, update_last_downloaded_filename
#test
# import datetime
from datetime import datetime

def main(save_path, from_station, to_station, date, time_str, direct):
    config_path = os.path.join(save_path, "config.json")
    config = load_config(config_path)

    filename = f"{from_station}-{to_station}-{date}-{time_str}-{direct}_site.html"
    filepath = os.path.join(save_path, filename)

    last_file = get_last_downloaded_filename(config)

    if last_file == filename and os.path.exists(filepath):
        print("Using cached file:", filename)
        with open(filepath, 'rb') as f:
            content = f.read()
    else:
        if last_file:
            old_filepath = os.path.join(save_path, last_file)
            if os.path.exists(old_filepath):
                os.remove(old_filepath)
                print("Removed old file:", last_file)

        url = generate_url(from_station, to_station, date, time_str)
        header = {'User-Agent': random.choice(user_agents)}
        r = requests.get(url, headers=header)

        if r.status_code != 200:
            print(f"Error downloading site: {r.status_code}")
            return []

        content = r.content
        save_html(content, filepath)

        update_last_downloaded_filename(config_path, filename)
        print("Downloaded and saved new fi1e:", filename)

    soup = BeautifulSoup(content, 'html.parser')
    rows = soup.select("tbody tr")

    id = 0
    results = []

    # print(rows[4].select('.prognosis'))

    for row in rows:
        table = ParseTable(row)
        if not table.direct_check(direct):
            id += 1
            table_data = {
                'id': id,
                **table.to_dict()
            }
            results.append(table_data)

    json_path = os.path.join(save_path, "rozklad.json")
    with open(json_path, 'w', encoding='utf-8') as f:
        json.dump(results, f, indent=4, ensure_ascii=False)

    return results

# main('D:/Programowanie/android/NextTrain/app/src/main/python/', 'Warszawa Centralna', 'Szczecin Główny', datetime.today().strftime('%d.%m.%y'), datetime.today().strftime('%H:%m'), False)
main('D:/Programowanie/android/NextTrain/app/src/main/python/', 'Warszawa Centralna', 'Szczecin', '23.04.25', '3:25', False)