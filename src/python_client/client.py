#!/usr/bin/python3


import socket
import sys

HOST = ""
PORT = ""

helpText = """
1 <roomname> - create room
2 <userId> <roomId> - add user to room
3 <userId> <roomId> - remove user to room
4 <userId> <roomId> - block user to room
5 <roomId> - print contents of room
6 <roomId> <messageId> - like message
7 <roomId> <messageId> - dislike message
8 - print stats
9 <roomId> <additional roomId (optional)> <parentId or 0> - create message
"""

def sendSocket(message):
    soc = socket.create_connection((HOST,PORT))
    soc.send(bytes(message, "utf-8"))

    return soc.recv(8192)

def sendRequest(**kwargs):
    message = ""
    userID = kwargs.pop("userID")
    if userID:
        message += str(userID)
    for methodName, argList in kwargs.values():
        message += ";" + methodName
            for arg in argList:
                message += ":" + str(arg)

    print("sending message: ")
    print(message)

    return sendSockets(message)



def createRoom(*args):
    pass

def addUser(*args):
    pass

def removeUser(*args):
    pass

def blockUser(*args):
    pass

def printRoom(*args):
    pass

def likeMessage(*args):
    pass

def dislikeMessage(*args):
    pass

def printStats(*args):
    pass

def createMessage(*args):
    pass

def main():
    HOST, PORT = parse_args()

    input_map = {
        1: createRoom,
        2: addUser,
        3: removeUser,
        4: blockUSer,
        5: printRoom,
        6: likeMessage,
        7: dislikeMessage,
        8: printStats,
        9: createMessage
    }
    
    while True:
        cmd = None
        try:
            cmd = input(": ")
        except Exception:
            sys.exit(1)
        if cmd == "?":
            print helpText
            continue

        try:
            num = int(cmd.split(" ")[0])
            func = input_map[num]
            func(cmd)
        except Exception:
            continue
        

        
