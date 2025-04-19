import re
from bs4 import BeautifulSoup

class ParseTable:
    def __init__(self, row):
        self.station = f"{row.select('p.timeline__content-station')[0].text.strip()}-{row.select('p.timeline__content-station')[1].text.strip()}"
        self.date = row.select_one('span.stime.search-results__item-date').text.strip()
        self.departure_time = row.select('span.stime.search-results__item-hour')[0].text.strip()
        self.arrival_time = row.select('span.stime.search-results__item-hour')[1].text.strip()
        self.travel_time = row.select_one('span.search-results__item-train-nr.txlc').text.strip()
        self.transfers = row.select_one('.col-6.box--flex--wrap.add-arrow-to-right-before div.row div.margin-top-10 strong').text.strip()
        transports = []
        for i in range(4):
            transports.append(row.select('.col-3.col-12--phone.inline-center.box--flex--column p')[i].text.strip())

        self.transport = {}
        self.transport.update({transports[0][:10]: transports[0][10:].strip()})
        self.transport.update({transports[1][:13]: transports[1][13:].strip()})
        self.transport.update({transports[2][:18]: transports[2][18:].strip()})
        if transports[3][:7] == "Relacja":
            self.transport.update({transports[3][:7]: transports[3][7:].strip()})
        elif transports[3][:12] == "Nazwa pociągu":
            self.transport.update({transports[3][:12]: transports[3][12:].strip()})

    def __str__(self):
        return (f"Stacja: {self.station}\n"
                f"Data: {self.date}\n"
                f"Odjazd: {self.departure_time}\n"
                f"Przyjazd: {self.arrival_time}\n"
                f"Czas przejazdu: {self.travel_time}\n"
                f"Ilość przesiadek: {self.transfers}\n"
                f"Środek transportu: {self.transport}"
                )