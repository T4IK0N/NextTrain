from urllib.parse import urlencode
import requests
import random

def get_station_codes(station_name):
    base_url = "https://rozklad-pkp.pl/station/search"
    params = {
        "term": station_name,
        "short": "false"
    }

    user_agents = [
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36', # Google Chrome
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Gecko/20100101 Firefox/78.0', # Mozilla Firefox
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.864.59 Safari/537.36 Edg/91.0.864.59', # Microsoft Edge
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Version/13.1 Safari/537.36', # Safari
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 OPR/77.0.4054.172', # Opera
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Brave/91.1.26.81', # Brave
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Vivaldi/4.0.2312.33', # Vivaldi
        'Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36 SamsungBrowser/14.0', # Samsung Internet
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Chromium/91.0.4472.124', # Chromium
    ]

    headers = {
        'User-Agent': random.choice(user_agents)
    }

    try:
        response = requests.get(base_url, params=params, headers=headers)
        response.raise_for_status()

        if not response.text.strip():
            print("Pusta odpowiedź z serwera.")
            return []

        stations = response.json()

        if not stations:
            print("Nie znaleziono żadnych stacji.")
            return []

        # for i, station in enumerate(stations):
        #     print(f"{i + 1}. {station['name']} - kod: {station['value']}")

        return stations[0]['value']

    except requests.RequestException as e:
        print(f"error http: {e}")
    except ValueError as e:
        print(f"json error: {e}")
    return []

def generate_url(from_station_name, to_station_name, travel_date, travel_time):
    from_station_code = get_station_codes(from_station_name)
    to_station_code = get_station_codes(to_station_name)

    if not from_station_code or not to_station_code:
        raise ValueError("Nieprawidłowa nazwa stacji")

    params = {
        "queryPageDisplayed": "yes",
        "REQ0JourneyStopsS0A": "1",
        "REQ0JourneyStopsS0G": from_station_code,
        "REQ0JourneyStopsS0ID": "",
        "REQ0JourneyStopsZ0A": "1",
        "REQ0JourneyStopsZ0G": to_station_code,
        "date": travel_date,
        "dateStart": travel_date,
        "dateEnd": travel_date,
        "REQ0JourneyDate": travel_date,
        "time": travel_time,
        "REQ0JourneyTime": travel_time,
        "REQ0HafasSearchForw": "1",
        "existBikeEverywhere": "yes",
        "existHafasAttrInc": "yes",
        "existHafasAttrExc": "yes",
        "start": "start",
        "came_from_form": "1",
        "singlebutton": "",
        "wDayExt0": "Pn|Wt|Śr|Cz|Pt|So|Nd",
        "REQ0JourneyStopsS0ID": "",
        "REQ0JourneyStopover1": "",
        "REQ0JourneyStops2.0G": "",
        "REQ0JourneyStopover2": "",
    }

    base_url = "https://rozklad-pkp.pl/pl/tp?"
    full_url = base_url + urlencode(params)

    return full_url

def save_html(html, path):
        with open(path, "wb") as f:
            f.write(html)


user_agents = [
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36', # Google Chrome
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Gecko/20100101 Firefox/78.0', # Mozilla Firefox
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.864.59 Safari/537.36 Edg/91.0.864.59', # Microsoft Edge
    'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Version/13.1 Safari/537.36', # Safari
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 OPR/77.0.4054.172', # Opera
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Brave/91.1.26.81', # Brave
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Vivaldi/4.0.2312.33', # Vivaldi
    'Mozilla/5.0 (Linux; Android 10; SM-G973F) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.120 Mobile Safari/537.36 SamsungBrowser/14.0', # Samsung Internet
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36 Chromium/91.0.4472.124', # Chromium
]