#!/usr/bin/python
import tornado.httpserver
import tornado.ioloop
import tornado.web

from handler import MainHandler

if __name__=="__main__":
	#Handle all urls using MainHandler, with the text following the slash being one parameter
	application = tornado.web.Application([ ("/(.*)",MainHandler) ])
	#Start up the server on port 8042 and begin an IO loop with it
	server = tornado.httpserver.HTTPServer(application)
	server.listen(8042)
	tornado.ioloop.IOLoop.instance().start()
