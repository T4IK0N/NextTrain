import re
from bs4 import BeautifulSoup

class ParseTable:
    def __init__(self, row):
        self.station = f"{row.select('.clear-lowres')[0].text.strip()}-{row.select('.clear-lowres')[1].text.strip()}"
        self.date = row.select('td')[2].text.strip()
        self.departure_time = row.select('.clear-lowres')[2].select('span')[2].text.strip()
        prognosis_element = row.select_one('.prognosis')

        if prognosis_element is not None:
            span_element = prognosis_element.select_one('span')
            if span_element:
                # self.delay = span_element.text.strip()
                # if self.delay.startswith("ok. "):
                self.delay = span_element.text.strip()[:-1] #dot
            else:
                img_element = prognosis_element.select_one('img')
                if img_element:
                    self.delay = None
                else:
                    self.delay = None
        else:
            self.delay = "nie ma prognosis"

        self.arrival_time = row.select('.clear-lowres')[3].select('span')[2].text.strip()
        self.travel_time = row.select('td')[4].text.strip()
        self.transfers = row.select('td')[5].text.strip()
        transports = row.select('td')[6].select('img')

        transport_list = [
            {"src": "eip_pic.gif", "transport": "EIP"},
            {"src": "eic_pic.gif", "transport": "EIC"},
            {"src": "icp_pic.gif", "transport": "IC"},
            {"src": "tlk_pic.gif", "transport": "TLK"},
            {"src": "transfer_pic.gif", "transport": "Pieszo (transfer pieszy)"},
            {"src": "km_pic.gif", "transport": "Koleje Mazowieckie"},
            {"src": "reg_pic.gif", "transport": "REGIO"}
        ]

        self.transport_names = [
            transport["transport"] for img in transports
            for transport in transport_list if img["src"].endswith(transport["src"])
        ]

    def direct_check(self, direct):
        if direct and int(self.transfers) > 0:
            return True
        else:
            return False

    def to_dict(self):
        return {
            "station": self.station,
            "date": self.date,
            "departure_time": self.departure_time,
            "arrival_time": self.arrival_time,
            "travel_time": self.travel_time,
            "transfers": int(self.transfers),
            "transport": self.transport_names,
            "delay": self.delay
        }
        
    def __str__(self):
        return (
            f"Stacja: {self.station}\n"
            f"Data: {self.date}\n"
            f"Odjazd: {self.departure_time}\n"
            f"Przyjazd: {self.arrival_time}\n"
            f"Czas przejazdu: {self.travel_time}\n"
            f"Ilość przesiadek: {int(self.transfers)}\n"
            f"Środek transportu: {self.transport_names}\n"
            f"Delay: {self.delay}"
        )