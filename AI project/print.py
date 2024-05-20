import speech_recognition as sr
import pywhatkit as pt
import pyautogui
import time as t
from jarvis_hud import take_command
from jarvis_hud import talk



def printfun(opt):
    file_name = ""
    
    while file_name=="":
        talk("what name should I give to this file sir")
        file_name = take_command()
        if "close" in file_name or "stop" in file_name or "cancel" in file_name:
            talk("ok sir I will cancel this printing")
            pyautogui.press("esc")
            t.sleep(1)
            if opt=="nrmlprint":
                pass
            elif opt=="noteprint":
                pyautogui.press("enter")
               
        elif file_name!="" :
            pyautogui.write(file_name)
            pyautogui.press("enter")
            talk("successfully printed sir")
           
def fileopn():
    talk("opening  new file sir")
    pyautogui.hotkey("ctrl","o")
    file_name = ""
    while file_name=="":
        talk("which file would you like to open sir")
        file_name = take_command()
        if file_name!="":
            pyautogui.write(file_name)
            t.sleep(1)
            talk(file_name + "file opened sir")
            t.sleep(1)
            pyautogui.press("enter")
        elif "close" in file_name or "stop" in file_name or "cancel" in file_name:
            talk("ok sir ")
            t.sleep(1)
            pyautogui.press("esc")
            t.sleep(1)