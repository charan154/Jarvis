import speech_recognition as sr
import pyttsx3
import pywhatkit as pt
import datetime
import wikipedia
import os
import smtplib
import pyautogui
import time as t
import webbrowser
from jarvis_hud import start_hud
import threading
from typing_1 import typingtext
from jarvis_hud import talk
from jarvis_hud import take_command,print_statement
import sys
from threading import Thread,Timer

global text_widget
is_talking = False
is_listening = False
global hud_running
hud_running = True
listener = sr.Recognizer()
engine = pyttsx3.init()

def timewish():
    now = datetime.datetime.now()
    time_24hrs = now.strftime("%H:%M")
    time_12hrs = now.strftime("%I:%M %p")
    hour = now.hour

    talk("Sir the current time is " + time_12hrs)
    
    if 4 <= hour < 12:
        talk("Good Morning Sir")
    elif 12 <= hour < 16:
        talk("Good Afternoon Sir")
    elif 16 <= hour < 21:
        talk("Good Evening Sir")
    else:
        talk("Good Night Sir, have a sound sleep")

def wish():
    talk("Welcome Back Charan")
    t.sleep(1)
    timewish()
    t.sleep(1)
    talk("How can i help you right now")
    t.sleep(1)


 


import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

def sendEmail(to, content):
    server = smtplib.SMTP('smtp.gmail.com', 587)
    server.starttls()  # Use TLS/SSL for secure connection
    server.login('20r01a6602@cmritonline.ac.in', 'nxsm ocpz hcsz pkyd')  # Replace with your email and password

    msg = MIMEMultipart()
    msg['From'] = '20r01a6602@cmritonline.ac.in'
    msg['To'] = to
    msg['Subject'] = 'An important message'

    body = content
    msg.attach(MIMEText(body, 'plain'))

    text = msg.as_string()
    server.sendmail('20r01a6602@cmritonline.ac.in', to, text)
    server.quit()


def run_jarvis():
    t1 = threading.Thread(target=start_hud)
    t1.start()
    t.sleep(26)
    wish()
    t.sleep(1)
    global text_widget
    global is_talking
    global is_listening
    while True:
        

        command = take_command()
        
        if command is not None:
            if "in youtube" in command:
                song = command.replace("play in youtube"," ")
                talk("Playing" + song)
                pt.playonyt(song)
                
            elif "time" in command and "stamp" not in command:
                time = datetime.datetime.now().strftime("%I:%M %p")
                talk("Sir the current time is " +time)
                

            elif ' about ' in command :
                person = command.replace('tell me about', '').strip()
                try:
                    info = wikipedia.summary(person, sentences=3)
                    talk("Retrieving information about " + person)
                    talk(info)
                except wikipedia.exceptions.PageError:
                    talk("I'm sorry, I couldn't find any information about " + person)
                except wikipedia.exceptions.DisambiguationError:
                    talk("I'm sorry, there are multiple options for " + person + ". Please be more specific.")
                
                


            elif "search for" in command:
                query = command.replace('search'," ")
                talk("searching for "+ query +"in Google")
                pt.search(query)

            elif "email" in command:
                to = command.replace("send an email to "," ")
                try:
                    send = True
                    content_info=True
                    content=""
                    while send:
                        talk("To whom shall I Send sir")
                        to = take_command().strip()
                        to = to.replace(" ","")
                        to = to+"@gmail.com"
                        print_statement(to)
                        talk("Is this an email you want to send "+to)
                        res = take_command()
                        if "yes" in res or "yeah" in res or "send" in res:
                            talk("ok sir proceeding to  an add content to this email"+to)
                            send = False
                        else:
                            talk("ok sir as your wish")
                    while content_info:

                        talk("What should I add to content sir" )
                        content_res = take_command()
                        if "enough content" in content_res:
                            talk("ok sir I will proceed to send")
                            sendEmail(to,content)
                            talk("Sending an email to " + to)
                            t.sleep(1)
                            talk("An email has been sent to" +to)
                            content_info=False
                        content = content+content_res +" "
                        
                except Exception as e:
                    print_statement(e)
                    talk("Sorry sir I have failed to send an email to" +to)

            elif "shut down" in command or "shutdown" in command:
                while True:
                    talk("Are you sure you want to shut down your computer?")
                    ans = take_command()
                    print_statement(ans)
                    if "yes" in ans or "yeah" in ans or "proceed" in ans :
                        talk("Initializing shut down sequence sir!")
                        pt.shutdown(time=5)
                    elif "no" in ans or "dont" in ans :
                        talk("ok sir! Cancelling shut down sequence.")
                        pt.cancelshutdown()
                        break
                    elif "cancel" in ans:
                        break
                    else:
                        talk("Sorry, I didn't understand. Please say 'yes' or 'no'.")

            elif "open new file" in command:
                from print import fileopn
                fileopn()     

            elif "open" in command:
                app = command.replace("open","").strip()
                
                talk("opening "+ app)
                if ".com" in app or ".co.in" in app or ".org" in app or ".in" in app:
                    webbrowser.open(f"https://www.{app}")
                    t.sleep(1)
                else:
                    pyautogui.press("win")
                    t.sleep(1)
                    pyautogui.write(app)
                    t.sleep(0.5)
                    pyautogui.press("enter")
                    t.sleep(1)
                    if app=="chrome" or app=="google chrome":
                        pyautogui.moveTo(647,410)
                        pyautogui.click()
                    elif app=="whatsapp":
                        from typing_1 import wtsup_controls
                        wtsup_controls()
            

            elif "previous tab" in command:
                talk("moving previous")
                pyautogui.hotkey("alt","shift","tab")

            elif "next tab" in command and "item" not in command:
                talk("moving next")
                pyautogui.hotkey("alt","tab")
            elif "scroll down" in command:
                talk("scrolling down sir")
                pyautogui.scroll(-500)
            
            elif "scroll up" in command:
                talk("scrolling up sir")
                pyautogui.scroll(500)


            elif "create new file" in command:
                talk("ok sir i will create a new file")
                pyautogui.hotkey("ctrl","n")

            elif "save" in command and "as" not in command:
                talk("ok sir i will save this file for you")
                t.sleep(1)
                pyautogui.hotkey("ctrl","s")
                talk("yeah sir I have saved this file")

            elif "type" in command or "start typing" in command :
                from typing_1 import typingtext
                typingtext()
            

            elif "give backspace" in command:
                pyautogui.hotkey("backspace")
            elif "delete word" in command:
                pyautogui.hotkey("ctrl","backspace")
            elif "erase everything" in command or "delete all" in command:
                pyautogui.hotkey("ctrl","a")
                t.sleep(0.5)
                pyautogui.press("backspace")
            
            elif "save as" in command:
                pyautogui.hotkey("ctrl","shift","s")
                talk("proceeding to save sir")
                t.sleep(1)
                while True:

                    talk("what name should I give to this file sir")
                    filename = take_command()
                    if "cancel" in filename or "stop" in filename:
                        break
                    else:
                        pyautogui.write(filename)
                        pyautogui.press("enter")
                        talk("yeah sir I have saved this file")
            elif "back" in command:
                pyautogui.hotkey("ctrl","left")
                talk("moved back sir")
            elif "front" in command:
                pyautogui.hotkey("ctrl","right")
                talk("moved front sir")
            elif "new window" in command:
                talk("creating a new window sir")
                pyautogui.hotkey("ctrl","shift","n")
            elif "print this file" in command:
                print_state = True
                talk("prodeecing to print sir")
                pyautogui.hotkey("ctrl","p")
                t.sleep(0.5)
                pyautogui.press("tab")
                t.sleep(0.1)
                pyautogui.press("tab")
                t.sleep(0.1)
                pyautogui.press("tab")
                t.sleep(0.1)
                pyautogui.press("tab")
                t.sleep(0.1)
                pyautogui.press("tab")
                t.sleep(0.1)
                pyautogui.press("tab")
                t.sleep(0.1)
                pyautogui.press("enter")
                t.sleep(0.5)
                opt1 = "noteprint"
                from print import printfun
                printfun(opt1)
                
            elif "print" in command:
                opt = "nrmlprint"
                talk("proceeding to print sir")
                pyautogui.hotkey("ctrl","p")
                t.sleep(1)
                pyautogui.press("enter")
                t.sleep(0.5)
                from print import printfun
                printfun(opt)
            elif "close this window" in command:
                talk("closing current window sir")
                pyautogui.hotkey("ctrl","shift","w")
            elif "close this tab" in command:
                talk("closing current tab sir")
                pyautogui.hotkey("ctrl","w")
            elif "reload this page" in command:
                talk("reloading this page sir")
                pyautogui.hotkey("ctrl","r")     
            elif "page source" in command:
                talk("wait a moment sir")
                t.sleep(1)
                pyautogui.hotkey("ctrl","u")

            elif "close this app" in command:
                app = command.replace("close","")
                pyautogui.hotkey("alt","f4")
                talk("closing" + app)
            elif "track" in command:
                print_statement(pyautogui.position())
            elif "minimise "  in command or "minimize" in command:
                talk("ok sir")
                pyautogui.hotkey("win","down")
                pyautogui.hotkey("win","down")
            elif "show downloads" in command:
                talk("wait a moment sir")
                pyautogui.hotkey("ctrl","j")
            elif "find" in command:
                keyword = ""
                talk("ok sir wait a moment")
                pyautogui.hotkey("ctrl","f")
                while keyword=="":
                    talk("tell me the keyword to find")
                    keyword = take_command()
                pyautogui.write(keyword)
                pyautogui.press("enter")
            elif "next item" in command:
                pyautogui.hotkey("f3")
            elif "stop finding" in command:
                talk("ok sir")
                pyautogui.press("esc")
            elif "time stamp" in command:
                talk("ok sir wait a moment")
                pyautogui.hotkey("f5")
            elif "pause" in command or "resume" in command :
                pyautogui.press("k")
            
            elif "full screen" in command:
                pyautogui.press("f")
            elif "subtitles" in command:
                pyautogui.press("c")
            elif "play backward" in command:
                pyautogui.press("left")
            elif "play forward" in command:
                pyautogui.press("right")
            elif "tap on search" in command:
                pyautogui.press("/")
                searchcnt=""
                while searchcnt=="":
                    talk("what should i search sir")
                    searchcnt = take_command()
                    pyautogui.write(searchcnt)
                    pyautogui.press("enter")
            elif "screenshot" in command:
                talk("wait a moment sir I will take screenshot")
                pyautogui.hotkey("win","prtsc")
            elif "picture" in command:
                talk("ok sir taking picture in 3 seconds adjust your camera as required")
                t.sleep(3)
                pyautogui.press("enter")
                t.sleep(1)
            elif "video" in command:
                talk("ok sir wait a moment")
                t.sleep(1)
                pyautogui.press("up")
                talk("Initiating video recording sequence in 3 seconds sir")
                t.sleep(3)
                talk("starting video reconding")
                pyautogui.press("enter")
            elif "stop recording" in command:
                pyautogui.press("enter")
            elif "press enter" in command:
                pyautogui.press("enter")
            elif "press delete" in command:
                pyautogui.press("delete")
            elif "quit" in command or "stop" in command :
                talk("quiting programme sir!")
                hud_running = False
                sys.exit(0)
                
            elif "start recording" in command or "stop recording" in command:
                talk("I will start recording wait a moment sir")
                t.sleep(1)
                pyautogui.hotkey("alt","s")
            
            else:
                talk("unrecognized command sir")

            
        else:
            talk("Say that command again")
            time.sleep(1)


run_jarvis()

        