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
9 <roomId> <parentId or 0> - create message
10 <srcRoomID> <destRoomID> <msgID> - copy a message
"""

def sendSocket(message):
    soc = socket.create_connection((HOST,PORT))
    soc.send(bytes(message, "utf-8"))

    return soc.recv(8192).decode("utf-8")

def sendRequest(args):
    global USERID
    message = str(USERID)
    for arg in args:
        for methodName, methodArgs in arg.items():
            message += ";" + methodName
            if isinstance(methodArgs, list):
                for methodArg in methodArgs:
                    message += ":" + str(methodArg)
            else:
                message += ":" + str(methodArgs)

    message += '\n'

    return sendSocket(message)

def create_user(name):
    args = [
        { "createUser": name }
    ]
    response = sendRequest(args)
    my_id = response.split(":")[-1]
    return response.split(":")[-1]

def parse_args():
    host = sys.argv[1]
    port = sys.argv[2]
    name = sys.argv[3]

    print((host, port, name))
    return (host, port, name)

def createRoom(cmd):
    args = [
        { "createChatRoom": cmd[1] }
    ]
    return sendRequest(args)

def addUser(cmd):
    roomId = int(cmd[2])
    args = [
        { "getRemoteChatroom": roomId }, 
        { "addUser": cmd[1] }
    ]
    return sendRequest(args)

def removeUser(cmd):
    roomId = int(cmd[2])
    args = [
        { "getRemoteChatroom": roomId }, 
        { "removeUser": cmd[1] }
    ]
    return sendRequest(args)

def blockUser(cmd):
    roomId = int(cmd[2])
    args = [
        { "getRemoteChatroom": roomId }, 
        { "blockUser": cmd[1] }
    ]
    return sendRequest(args)

def printRoom(cmd):
    roomId = int(cmd[1])
    args = [
        { "getRemoteChatroom": roomId }, 
        { "getMessages": None }
    ]
    return sendRequest(args)

def likeMessage(cmd):
    roomId = int(cmd[1])
    msgId = int(cmd[2])
    args = [
        { "getRemoteChatroom": roomId }, 
        { "likeMessage": msgId }
    ]
    return sendRequest(args)

def dislikeMessage(cmd):
    roomId = int(cmd[1])
    msgId = int(cmd[2])
    args = [
        { "getRemoteChatroom": roomId }, 
        { "dislikeMessage": msgId }
    ]
    return sendRequest(args)

def printStats(cmd):
    args = [
        { "printStats": USERID }
    ]
    return sendRequest(args)

def createMessage(cmd):
    function_num = cmd.pop(0)
    roomId = int(cmd.pop(0))
    parentId = int(cmd.pop(0))
    msg = " ".join(cmd)
    args = [
        { "getRemoteChatroom": roomId }, 
        { "createMessage": [USERID, parentId, msg] }
    ]
    return sendRequest(args)

def main():
    global HOST, PORT, USERID
    HOST, PORT, username = parse_args()

    my_id = create_user(username)
    USERID = my_id.strip()

    print("You are user: " + USERID)

    input_map = {
        1: createRoom,
        2: addUser,
        3: removeUser,
        4: blockUser,
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
            print(helpText)
            continue

        try:
            cmd = cmd.split(" ")
            num = int(cmd[0])
            func = input_map[num]
            print(func(cmd))
        except Exception as e:
            print(e)


if __name__ == "__main__":
    main()
