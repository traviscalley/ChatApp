#!/usr/bin/python3


import socket
import sys

HOST = ""
PORT = ""

USERID = -1

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

def sendRequest(args):
    message = str(USERID)
    for arg in args:
        for methodName, args in kwargs.values():
            message += ";" + methodName
                if isinstance(args, list):
                    for arg in argList:
                        message += ":" + str(arg)
                else:
                    message += str(arg)

    print("sending message: ")
    print(message)

    return sendSockets(message)

def create_user(name):
    kwargs = [
        { "createUser": name }
    ]
    response = sendRequest(args)
    return response.split(":")[-1]

def parse_args():
    host = sys.argv[1]
    port = sys.argv[2]
    name = sys.argv[3]

    return (host, port, name)

def createRoom(cmd):
    args = [
        { "createChatroom": cmd[1] }
    ]
    return sendRequest(args)

def addUser(cmd):
    roomId = int(cmd[2])
    args = [
        { "getRemoteChatRoom": roomId }, 
        { "addUser": cmd[1] }
    ]
    return sendRequest(args)

def removeUser(cmd):
    roomId = int(cmd[2])
    args = [
        { "getRemoteChatRoom": roomId }, 
        { "removeUser": cmd[1] }
    ]
    return sendRequest(args)

def blockUser(cmd):
    roomId = int(cmd[2])
    args = [
        { "getRemoteChatRoom": roomId }, 
        { "blockUser": cmd[1] }
    ]
    return sendRequest(args)

def printRoom(cmd):
    roomId = int(cmd[1])
    args = [
        { "getRemoteChatRoom": roomId }, 
        { "getMessages": None }
    ]
    return sendRequest(args)

def likeMessage(cmd):
    roomId = int(cmd[1])
    msgId = int(cmd[2])
    args = [
        { "getRemoteChatRoom": roomId }, 
        { "likeMessage": mshId }
    ]
    return sendRequest(args)

def dislikeMessage(cmd):
    roomId = int(cmd[1])
    msgId = int(cmd[2])
    args = [
        { "getRemoteChatRoom": roomId }, 
        { "dislikeMessage": mshId }
    ]

def printStats(cmd):
    print "Not implemented"

def createMessage(cmd):
    roomId = int(cmd.pop(1))
    #parentId = int(cmd.pop(2))
    args = [
        { "getRemoteChatRoom": roomId }, 
        { "createMessage": cmd }
    ]

def main():
    HOST, PORT, username = parse_args()

    USERID = create_user(username)

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
            cmd = cmd.split(" ")
            num = int(cmd[0])
            func = input_map[num]
            print func(cmd)
        except Exception:
            continue


if __name__ == "__main__":
    main()
