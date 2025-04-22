import re
from bs4 import BeautifulSoup

class ParseTable:
    def __init__(self, row):
        self.station = f"{row.select('.clear-lowres')[0].text.strip()}-{row.select('.clear-lowres')[1].text.strip()}"
        self.date = row.select('td')[2].text.strip()
        self.departure_time = row.select('.clear-lowres')[2].select('span')[2].text.strip()
        prognosis_elements = row.select('.prognosis')

        prognosis_departure = prognosis_elements[0] if len(prognosis_elements) > 0 else None
        prognosis_arrival = prognosis_elements[1] if len(prognosis_elements) > 1 else None

        if prognosis_departure is not None:
            span_element = prognosis_departure.select_one('span')
            if span_element:
                self.delay_departure = span_element.text.strip()[4:-5] #ok. & dot
            else:
                img_element = prognosis_departure.select_one('img')
                if img_element:
                    self.delay_departure = None
                else:
                    self.delay_departure = None
        else:
            self.delay_departure = None #nie ma prognosis

        if prognosis_arrival is not None:
            span_element = prognosis_arrival.select_one('span')
            if span_element:
                self.delay_arrival = span_element.text.strip()[4:-5] #ok. & dot
            else:
                img_element = prognosis_arrival.select_one('img')
                if img_element:
                    self.delay_arrival = None
                else:
                    self.delay_arrival = None
        else:
            self.delay_arrival = None #nie ma prognosis

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

        self.href = None
    #     print(row.select('td')[7])
    #
    # def href_check(self, row):
    #     # if len(self.transport_names) == 1 and self.transport_names[0] == "REGIO":
    #     a_tag = row.select('td')[7].select_one('a')
    #     if a_tag and a_tag.has_attr('href'):
    #         return a_tag['href']
    #         # cut_index = href.find("*/")
    #         # if cut_index != -1:
    #         #     self.href = href[:cut_index]
    #     # if row.select('td')[7].select_one('a')

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
            "delay_departure": self.delay_departure,
            "delay_arrival": self.delay_arrival,
            "href_ticket": self.href
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
            f"Delay Departure: {self.delay_departure}\n"
            f"Delay Arrival: {self.delay_arrival}\n"
            f"Href Ticket: {self.href}"
        )