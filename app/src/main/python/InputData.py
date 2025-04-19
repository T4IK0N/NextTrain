from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
import time
import random

def generate_url(from_station, to_station, date_str, time_str, only_direct = False):
    options = webdriver.FirefoxOptions()
    # options.add_argument("--headless")  # bez okna
    driver = webdriver.Firefox(options=options)

    try:
        driver.get("https://portalpasazera.pl/pl/")

        time.sleep(random.uniform(2, 3))  # czas na załadowanie strony

        cookie = driver.find_element(By.CSS_SELECTOR, ".btn.cookieman-operation.full-width--phone.allow-all-submit.txuc")
        time.sleep(random.uniform(1, 2))
        cookie.click()

        from_input = driver.find_element(By.ID, "departureFrom")
        time.sleep(random.uniform(2, 3))
        from_input.send_keys(from_station)
        time.sleep(random.uniform(1, 2))
        from_input.send_keys(Keys.RETURN)

        to_input = driver.find_element(By.ID, "arrivalTo")
        time.sleep(random.uniform(2, 3))
        to_input.send_keys(to_station)
        time.sleep(random.uniform(1, 2))
        to_input.send_keys(Keys.RETURN)

        date_input = driver.find_element(By.ID, "main-search__dateStart")
        date_input.clear()
        time.sleep(random.uniform(1, 2))
        date_input.send_keys(date_str)

        time_input = driver.find_element(By.ID, "main-search__timeStart")
        time_input.clear()
        time.sleep(random.uniform(2, 3))
        time_input.send_keys(time_str)

        if only_direct:
            try:
                checkbox = driver.find_element(By.ID, "dirChck")
                if not checkbox.is_selected():
                    label = driver.find_element(By.CLASS_NAME, "main-search__connection-directCheck")
                    label.click()
                    time.sleep(random.uniform(0.5, 1.2))  # naturalna pauza
            except Exception as e:
                print("⚠️ Błąd przy ustawianiu direct-connection:", e)

        time.sleep(random.uniform(1, 2))
        time_input.send_keys(Keys.ENTER)

        time.sleep(random.uniform(1, 3))

        result_url = driver.current_url

        print("Wygenerowany URL:", result_url)
        return result_url

    finally:
        driver.quit()

# from_station = input("Stacja początkowa (np. Warszawa): ")
# to_station = input("Stacja końcowa (np. Białystok): ")
# date = input("Data (format: dd.mm.yy, np. 05.04.2025): ")
# time_str = input("Godzina (format: hh:mm, np. 13:50): ")
# generate_portal_pasazera_url(from_station, to_station, date, time_str)