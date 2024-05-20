import tkinter as tk
from PIL import Image, ImageTk
import os
os.environ['PYGAME_HIDE_SUPPORT_PROMPT'] = "hide"
import pygame
import pyttsx3
import pyautogui
import time as t
import speech_recognition as sr
import sys
import threading
import atexit
global hud_running
global text_widget
engine = pyttsx3.init()
engine.runAndWait()
is_talking = False
is_listening = False
def talk(text):
    global is_talking
    is_talking = True
    engine.say(text)
    print_statement(text)
    engine.runAndWait()
    is_talking = False

listener = sr.Recognizer()

def take_command():
    global listener
    global is_listening
    is_listening = True
    command = ''
    try:
        with sr.Microphone() as source:
            print_statement("Listening...")
            listener.pause_threshold = 1.0
            voice = listener.listen(source)
            command = listener.recognize_google(voice)
            print_statement(command)
            is_listening = False
            return command.lower()
        

    except sr.UnknownValueError:
        talk("Sorry, I couldn't understand that.")
    except sr.RequestError:
        talk("Sorry, my speech service is down.")
    except KeyboardInterrupt:
        talk("Interrupted by user")
    finally:
        
        listener = sr.Recognizer()

    return command

class RoundedLabel(tk.Label):
    def __init__(self, master=None, **kwargs):
        super().__init__(master, **kwargs)
        self.bind('<Configure>', self.set_radius)
        self.set_radius()

    def set_radius(self, event=None):
        self.configure(
            borderwidth=-10,
            highlightthickness=0,
            relief='solid',
            background='#02070B',
            padx=-30,
            pady=30,
            font=('Helvetica', 12),
        )

def animation(count, label, images, total_frames):
    im = images[count]
    label.configure(image=im)
    count += 1
    if count == total_frames:
        count = 0
    label.after(50, lambda: animation(count, label, images, total_frames))

def voice_animation(frame):
    global is_talking

    file2 = "./hud/voice1.gif"
    gif_image2 = Image.open(file2)
    frames2 = gif_image2.n_frames
    im2 = [tk.PhotoImage(file=file2, format=f'gif -index {i}') for i in range(frames2)]
    gif_label2 = RoundedLabel(frame, image='', bg="#0F0F2B")
    gif_label2.place(relx=1.0, rely=0.5, anchor="se")
    count2 = 0

    def animate():
        nonlocal count2
        if is_talking:
            gif_label2.configure(image=im2[count2])
            count2 = (count2 + 1) % frames2
        else:
            count2 = 0
            gif_label2.configure(image='')
        frame.after(50, animate)

    animate()

def listener_animation(frame):
    global is_listening
    file3 = "./hud/listener3.gif"
    gif_image3 = Image.open(file3)
    frames3 = gif_image3.n_frames
    im3 = [tk.PhotoImage(file=file3, format=f'gif -index {i}') for i in range(frames3)]
    gif_label3 = RoundedLabel(frame, image='', bg="#0F0F2B")
    gif_label3.place(relx=0.2, rely=0.5, anchor="se")
    count3 = 0

    def animate():
        nonlocal count3
        if is_listening:
            gif_label3.configure(image=im3[count3])
            count3 = (count3 + 1) % frames3
        else:
            count3 = 0
            gif_label3.configure(image='')
        frame.after(50, animate)

    animate()
text_widget = None

def print_statement(statement):
    if text_widget:
        text_widget.insert(tk.END, statement + "\n")
        text_widget.see(tk.END)



def start_hud():
    global hud_running
    global text_widget
    root = tk.Tk()
    root.title("Jarvis")

    file1 = "./hud/vaccum5.gif"
    gif_image1 = Image.open(file1)
    frames1 = gif_image1.n_frames
    im1 = [tk.PhotoImage(file=file1, format=f'gif -index {i}') for i in range(frames1)]



    frame = tk.Frame(root)
    frame.pack(fill="both", expand=True)

    gif_label1 = tk.Label(frame, image='')
    gif_label1.pack(fill="both", expand=True)



    count1 = 0
    animation(count1, gif_label1, im1, frames1)



    pygame.mixer.init()
    audio_file = "./hud/audio3.wav"
    pygame.mixer.music.load(audio_file)
    pygame.mixer.music.play()

    def on_closing():
        if threading.main_thread().is_alive():
            hud_running = False

            talk("closing sir")
        # root.destroy()
        pygame.mixer.music.stop()
        
        root.quit()
        # sys.exit(0)
    atexit.register(on_closing)
    root.protocol("WM_DELETE_WINDOW", on_closing)

    voice_animation(frame)
    listener_animation(frame)

    text_frame = tk.Frame(root, bg="#0F0F2B", bd=1, relief=tk.SUNKEN)
    text_frame.place(relx=1.0, rely=1.0, anchor="se", relwidth=0.3, relheight=0.2)

    global text_widget
    text_widget = tk.Text(text_frame, bg="black", fg="white", wrap=tk.WORD)
    text_widget.pack(fill="both", expand=True)
    print_statement("Hi Charan")

    root.mainloop()



