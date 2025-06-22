import requests
import json
import time
import random
import os
from bs4 import BeautifulSoup
from ParseTable import ParseTable
from Data import generate_url, save_html, user_agents
from ConfigManager import load_config, get_last_downloaded_filename, update_last_downloaded_filename
# test
# import datetime
from datetime import datetime


def download_and_cache(save_path, from_station, to_station, date, time_str, direct):
    config_path = os.path.join(save_path, "config.json")
    config = load_config(config_path)

    time_str_safe = time_str.replace(':', '.')
    filename = f"{from_station}-{to_station}-{date}-{time_str_safe}-{direct}_site.html"
    filepath = os.path.join(save_path, filename)

    last_file = get_last_downloaded_filename(config)

    # Usuń stary plik jeśli nazwa się zmieniła
    if last_file and last_file != filename:
        old_filepath = os.path.join(save_path, last_file)
        if os.path.exists(old_filepath):
            os.remove(old_filepath)
            print("Removed old file:", last_file)

    # Jeśli plik istnieje i jest aktualny - użyj cache
    if last_file == filename and os.path.exists(filepath):
        print("Using cached file:", filename)
        with open(filepath, 'rb') as f:
            content = f.read()
    else:
        url = generate_url(from_station, to_station, date, time_str)
        header = {'User-Agent': random.choice(user_agents)}
        r = requests.get(url, headers=header)

        if r.status_code != 200:
            print(f"Error downloading site: {r.status_code}")
            return None

        content = r.content
        save_html(content, filepath)
        update_last_downloaded_filename(config_path, filename)
        print("Downloaded and saved new file:", filename)

    return content


def parse_results(content, direct):
    soup = BeautifulSoup(content, 'html.parser')
    rows = soup.select("tbody tr")

    results = []
    id_counter = 0
    for row in rows:
        table = ParseTable(row)
        if not table.direct_check(direct):
            id_counter += 1
            table_data = {
                'id': id_counter,
                **table.to_dict()
            }
            results.append(table_data)
    return results


def main(save_path, from_station, to_station, date, time_str, direct):
    content = download_and_cache(save_path, from_station, to_station, date, time_str, direct)
    if content is None:
        return []

    results = parse_results(content, direct)

    json_path = os.path.join(save_path, "rozklad.json")
    with open(json_path, 'w', encoding='utf-8') as f:
        json.dump(results, f, indent=4, ensure_ascii=False)

    return results


def add_to_file(save_path, from_station, to_station, date, time_str, direct):
    content = download_and_cache(save_path, from_station, to_station, date, time_str, direct)
    if content is None:
        return []

    new_results = parse_results(content, direct)

    json_path = os.path.join(save_path, "rozklad.json")
    if os.path.exists(json_path):
        with open(json_path, 'r', encoding='utf-8') as f:
            existing_data = json.load(f)
    else:
        existing_data = []

    last_id = existing_data[-1]['id'] if existing_data else 0
    for i, train in enumerate(new_results, start=1):
        train['id'] = last_id + i

    updated_data = existing_data + new_results

    with open(json_path, 'w', encoding='utf-8') as f:
        json.dump(updated_data, f, indent=4, ensure_ascii=False)

    return new_results


def fetch_next_trains(save_path, from_station, to_station, direct):
    json_path = os.path.join(save_path, "rozklad.json")

    if not os.path.exists(json_path):
        print("rozklad.json not found!")
        return

    with open(json_path, 'r', encoding='utf-8') as f:
        existing_data = json.load(f)

    if not existing_data:
        print("rozklad.json is empty!")
        return

    last_train = existing_data[-1]

    last_date = last_train.get('date')
    last_time = last_train.get('departure_time')

    if not last_date or not last_time:
        print("Last train is missing date or departure time!")
        return

    print(f"Fetching new trains from {last_date} {last_time}...")

    new_trains = add_to_file(save_path, from_station, to_station, last_date, last_time, direct)

    if not new_trains:
        print("No new trains found.")
        return

    # Przefiltruj nowe pociągi, żeby nie dodać tych co już są
    existing_keys = {(train['departure_time'], train['arrival_time'], train['date'], train['travel_time']) for train in
                     existing_data}

    unique_new_trains = []
    for train in new_trains:
        key = (train.get('departure_time'), train.get('arrival_time'), train.get('date'), train.get('travel_time'))
        if key not in existing_keys:
            unique_new_trains.append(train)
        else:
            print(f"Skipping duplicate train: {key}")

    if not unique_new_trains:
        print("No unique new trains to add.")
        return

    # Przesuń ID
    last_id = existing_data[-1]['id']
    for idx, train in enumerate(unique_new_trains, start=1):
        train['id'] = last_id + idx

    # Zaktualizuj plik
    updated_data = existing_data + unique_new_trains

    with open(json_path, 'w', encoding='utf-8') as f:
        json.dump(updated_data, f, indent=4, ensure_ascii=False)

    print(f"Added {len(unique_new_trains)} unique new trains to rozklad.json.")
    return unique_new_trains

# main('D:/Programowanie/android/NextTrain/app/src/main/python/', 'Warszawa Centralna', 'Szczecin Główny', datetime.today().strftime('%d.%m.%y'), datetime.today().strftime('%H:%m'), False)
# fetch_next_trains('D:/Programowanie/android/NextTrain/app/src/main/python/', 'Warszawa Centralna', 'Szczecin Główny', False)
# fetch_next_trains('D:/Programowanie/android/NextTrain/app/src/main/python/', 'Warszawa Centralna', 'Szczecin Główny', False)
# fetch_next_trains('D:/Programowanie/android/NextTrain/app/src/main/python/', 'Warszawa Centralna', 'Szczecin Główny', False)
# main('D:/Programowanie/android/NextTrain/app/src/main/python/', 'Warszawa Centralna', 'Szczecin', '23.04.25', '3:25', False)