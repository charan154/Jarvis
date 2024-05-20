import speech_recognition as sr
import pywhatkit as pt
import pyautogui
import time as t
from jarvis_hud import take_command
from jarvis_hud import talk




def typingtext():
    while True:
        command = take_command()
        if "stop typing" in command:
            break
        elif "give space" in command:
            pyautogui.press("space")
        elif "give backspace" in command:
            pyautogui.hotkey("backspace")
        elif "delete word" in command:
            pyautogui.hotkey("ctrl","backspace")
        elif "erase everything" in command or "delete all" in command:
            pyautogui.hotkey("ctrl","a")
            t.sleep(0.5)
            pyautogui.press("backspace")
        elif "press back" in command:
            pyautogui.hotkey("ctrl","left")
            talk("moved back sir")
        elif "press front" in command:
            pyautogui.hotkey("ctrl","right")
            talk("moved front sir")
        
            
        else:
            pyautogui.write(command)
            pyautogui.press("space")
            t.sleep(2)
            talk("would you like to add more sir")
            t.sleep(0.5)
def user_contrals():
    while True:
        command = take_command()
        if "up" in command:
            pyautogui.hotkey("up")
        elif "down" in command:
            pyautogui.hotkey("down")
        elif "right" in command:
            pyautogui.hotkey("right")
        elif "left" in command:
            pyautogui.hotkey("left")
        elif "enter" in command or "open" in command:
            pyautogui.press("enter")
        elif "stop" in command or "cancel" in command:
            pyautogui.press("esc")
            break
def opnct():
    prsn=""
    
    while prsn=="":
        pyautogui.hotkey("ctrl","a")
        t.sleep(1)
        pyautogui.press("backspace")
        talk("whose chat shall I open sir")
        prsn = take_command()
        if prsn!="":
            pyautogui.write(prsn)
            t.sleep(1)
            pyautogui.press("tab")
            t.sleep(1)
            pyautogui.press("enter")
            talk("opening " + prsn + "chat")
        else:
            pass       



def wtsup_controls():
    while True:
        command = take_command()
        if "open chat" in command:
            pyautogui.hotkey("ctrl","f")
            t.sleep(1)
            
            opnct()
        elif "start typing" in command:
            typingtext()

        elif "open new chat" in command:
            talk("opening new chat")
            pyautogui.hotkey("ctrl","n")
            opnct()
        elif "close chat" in command or "close this chat" in command:
            talk("closing current chat sir")
            pyautogui.hotkey("ctrl","w")
        elif "create new group" in command:
            talk("Wait a moment sir")
            pyautogui.hotkey("ctrl","shift","n")
            
            def create_new_grp():
                creation =True
                while creation:
                    prsn = ""
                    talk("select members to be added to the group sir")
                    prsn = take_command()
                    

                    if "enough" in prsn:
                        pyautogui.press("tab")
                        t.sleep(1)
                        pyautogui.press("enter")
                        t.sleep(1)
                        pyautogui.press("tab")
                        t.sleep(1)
                        pyautogui.press("tab")
                        t.sleep(1)
                        grp_name=""
                        while grp_name=="":
                            talk("what shall I give for group name sir")
                            grp_name = take_command()
                            if grp_name!="":
                                pyautogui.write(grp_name)
                                t.sleep(1)
                                pyautogui.press("tab")
                                t.sleep(1)
                                pyautogui.press("tab")
                                t.sleep(1)
                                pyautogui.press("tab")
                                t.sleep(1)
                                while True:
                                    talk("Are you sure sir Do you want to create this group")
                                    ans = take_command()
                                    if "yes" in ans or "yeah" in ans:
                                        talk("ok sir proceeding to create this group")
                                        t.sleep(1)
                                        pyautogui.press("enter")
                                        talk("I have successfully created this new group sir")
                                        creation = False
                                        pyautogui.press("esc")
                                        break
                                    elif "no" in ans:
                                        talk("Cancelling creation of this group sir")
                                        pyautogui.press("esc")
                                        t.sleep(1)
                                        pyautogui.press("enter")
                                        break

                    elif "stop creation" in prsn or "stop creating" in prsn:
                        talk("ok sir")
                        creation = False
                        break

                    elif prsn !="":
                        pyautogui.write(prsn)
                        pyautogui.press("tab")
                        t.sleep(1)
                        pyautogui.press("enter")
                        t.sleep(1)
                        pyautogui.press("tab")
                        t.sleep(1)
                        pyautogui.hotkey("ctrl","a")
                        pyautogui.press("backspace")
                        t.sleep(1)
            create_new_grp()

        elif "search" in command:
            talk("wait a moment sir")
            pyautogui.hotkey("ctrl","shift","f")
            talk("what keyword should I search sir")
            key_wrd=""
            while key_wrd=="":
                key_wrd = take_command()
                t.sleep(1)
                if key_wrd=="":
                    pass
                elif key_wrd!="":
                    pyautogui.write(key_wrd)
                    break
        elif "profile" in command:
            pyautogui.hotkey("ctrl","p")
            user_contrals()
        elif "mute" in command:
            talk("muting this chat sir")
            pyautogui.hotkey("ctrl","shift","m")
        elif "emoji" in command:
            talk("opening Emoji panel sir")
            pyautogui.hotkey("ctrl","shift","e")
        elif "gif" in command:
            talk("opening all gif sir")
            pyautogui.hotkey("ctrl","shift","g")
        elif "sticker" in command:
            talk("opening all stickers sir")
            pyautogui.hotkey("ctrl","shift","s")
        elif "previous chat" in command:
            talk("opening previous chat sir")
            pyautogui.hotkey("ctrl","shift","tab")
        elif "next chat" in command:
            talk("opening next chat sir")
            pyautogui.hotkey("ctrl","tab")


        elif "send" in command:
            pyautogui.press("enter")



        elif "close" in command:
            talk("closing whatsapp sir")
            pyautogui.hotkey("alt","f4")
            talk("done")
            break