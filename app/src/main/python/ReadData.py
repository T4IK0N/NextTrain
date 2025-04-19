import requests
import time
import random
from bs4 import BeautifulSoup
import re
from ParseTable import ParseTable
# from InputData import generate_url

# url = 'https://rozklad-pkp.pl/pl/tp?queryPageDisplayed=yes&REQ0JourneyStopsS0A=1&REQ0JourneyStopsS0ID=&REQ0JourneyStops1.0G=&REQ0JourneyStopover1=&REQ0JourneyStops2.0G=&REQ0JourneyStopover2=&REQ0JourneyStopsZ0A=1&REQ0JourneyStopsZ0ID=&REQ0HafasSearchForw=1&existBikeEverywhere=yes&existHafasAttrInc=yes&existHafasAttrInc=yes&REQ0JourneyProduct_prod_section_0_0=1&REQ0JourneyProduct_prod_section_1_0=1&REQ0JourneyProduct_prod_section_2_0=1&REQ0JourneyProduct_prod_section_3_0=1&REQ0JourneyProduct_prod_section_0_1=1&REQ0JourneyProduct_prod_section_1_1=1&REQ0JourneyProduct_prod_section_2_1=1&REQ0JourneyProduct_prod_section_3_1=1&REQ0JourneyProduct_prod_section_0_2=1&REQ0JourneyProduct_prod_section_1_2=1&REQ0JourneyProduct_prod_section_2_2=1&REQ0JourneyProduct_prod_section_3_2=1&REQ0JourneyProduct_prod_section_0_3=1&REQ0JourneyProduct_prod_section_1_3=1&REQ0JourneyProduct_prod_section_2_3=1&REQ0JourneyProduct_prod_section_3_3=1&REQ0JourneyProduct_opt_section_0_list=0%3A000000&REQ0HafasOptimize1=&existOptimizePrice=&REQ0HafasChangeTime=0%3A1&existSkipLongChanges=0&REQ0HafasAttrExc=&existHafasAttrInc=yes&existHafasAttrExc=yes&wDayExt0=Pn%7CWt%7C%C5%9Ar%7CCz%7CPt%7CSo%7CNd&start=start&existUnsharpSearch=yes&singlebutton=&came_from_form=1&REQ0JourneyStopsS0G=%C5%81apy+osse&REQ0JourneyStopsZ0G=5196021&date=07.04.25&dateStart=07.04.25&dateEnd=07.04.25&REQ0JourneyDate=07.04.25&time=06%3A00&REQ0JourneyTime=06%3A00'
# url = 'https://rozklad-pkp.pl/pl/tp?queryPageDisplayed=yes&REQ0JourneyStopsS0A=1&REQ0JourneyStopsS0G=5100065&REQ0JourneyStopsS0ID=&REQ0JourneyStops1.0G=&REQ0JourneyStopover1=&REQ0JourneyStops2.0G=&REQ0JourneyStopover2=&REQ0JourneyStopsZ0A=1&REQ0JourneyStopsZ0G=5196021&REQ0JourneyStopsZ0ID=&date=05.04.25&dateStart=05.04.25&dateEnd=05.04.25&REQ0JourneyDate=05.04.25&time=11%3A28&REQ0JourneyTime=11%3A28&REQ0HafasSearchForw=1&existBikeEverywhere=yes&existHafasAttrInc=yes&existHafasAttrInc=yes&REQ0JourneyProduct_prod_section_0_0=1&REQ0JourneyProduct_prod_section_1_0=1&REQ0JourneyProduct_prod_section_2_0=1&REQ0JourneyProduct_prod_section_3_0=1&REQ0JourneyProduct_prod_section_0_1=1&REQ0JourneyProduct_prod_section_1_1=1&REQ0JourneyProduct_prod_section_2_1=1&REQ0JourneyProduct_prod_section_3_1=1&REQ0JourneyProduct_prod_section_0_2=1&REQ0JourneyProduct_prod_section_1_2=1&REQ0JourneyProduct_prod_section_2_2=1&REQ0JourneyProduct_prod_section_3_2=1&REQ0JourneyProduct_prod_section_0_3=1&REQ0JourneyProduct_prod_section_1_3=1&REQ0JourneyProduct_prod_section_2_3=1&REQ0JourneyProduct_prod_section_3_3=1&REQ0JourneyProduct_opt_section_0_list=0%3A000000&existHafasAttrExc=yes&REQ0HafasChangeTime=0%3A1&existSkipLongChanges=0&REQ0HafasAttrExc=&REQ0HafasAttrExc=&REQ0HafasAttrExc=&REQ0HafasAttrExc=&REQ0HafasAttrExc=&REQ0HafasAttrExc=&REQ0HafasAttrExc=&REQ0HafasAttrExc=&REQ0HafasAttrExc=&REQ0HafasAttrExc=&REQ0HafasAttrExc=&REQ0HafasAttrExc=&existHafasAttrInc=yes&existHafasAttrExc=yes&wDayExt0=Pn%7CWt%7C%C5%9Ar%7CCz%7CPt%7CSo%7CNd&start=start&existUnsharpSearch=yes&came_from_form=1&singlebutton=#focus'
def main(save_path):
    url = 'https://portalpasazera.pl/pl/WynikiWyszukiwania?id=R0FQU0tJyteMLfoscc2FwaK1L3tNMkSenf6wTgdmpdm1mxbGxXm8scc2BNy1hp4UuvVeKNtDIOvbEscc2FYpzdYHl8vyscc2BKcMgYGxWVR1aEhXKQBmLGscc2F8PSQh9G8WFscc2Fp7G1Mscc2BSIroscc2FYML0YIzgQmmjvsAwUUHuBqscc2BxyafByscc2BfH2rscc2BZTdEi1rleip1jD31JPWnoIUjZscc2Flscc2Bscc2BAmjDIp7wBaX'

    # from_station = input("Wyjazd z: ")
    # to_station = input("Przyjazd do: ")
    # date = input("Data (format: dd.mm.yyyy): ")
    # time_str = input("Godzina (format: hh:mm): ")
    # only_direct = input("Połączenia bezpośrednie?: ")
    # url = generate_url(from_station, to_station, date, time_str, only_direct)
    # url = generate_url("warszawa", "białystok", "30.04.2025", "13:00", "True")

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

    header = {
        'User-Agent': random.choice(user_agents)
    }

    r = requests.get(url, headers=header)

    if r.status_code == 200:
        print("Strona została pobrana pomyślnie")
    else:
        print(f"Nie udało się pobrać strony, status: {r.status_code}")

    soup = BeautifulSoup(r.content, 'html.parser')
    rows = soup.select('.search-results__container .search-results__item.row.abt-focusable')

    results = []
    x = 0
    for row in rows:
        table = ParseTable(row)
        x += 1
        results.append(f"{x}\n{table}")

    def save_html(html, path):
        with open(path, "wb") as f:
            f.write(html)

    save_html(r.content, f"{save_path}/rozklad_pkp_pl_tp.html")

    return results