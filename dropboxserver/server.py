#!/usr/bin/python
import tornado.httpserver
import tornado.ioloop
import tornado.web

import optparse
import os.path
import sys
import socket

from handler import MainHandler,read_passwordfile

#Default options
WEBROOT = "webroot"
PORT = 8042

if __name__=="__main__":
	parser = optparse.OptionParser()
	parser.add_option('--webroot','-w',default=WEBROOT)
	parser.add_option('--port','-p',default=PORT,type=int)
	opts = parser.parse_args()[0]
	if not os.path.isdir(opts.webroot):
		print "Error: webroot '%s' does not exist" % opts.webroot
		sys.exit()
	MainHandler.WEBROOT = opts.webroot
	try:
		read_passwordfile()
	except Exception,e:
		print e
		sys.exit()
	print "Running DBox server on port %i with a web root of '%s' (see `./server.py -h` for options)." % (opts.port,opts.webroot)

	#Handle all urls using MainHandler, with the text following the slash being one parameter
	application = tornado.web.Application([ ("/(.*)",MainHandler) ])
	#Start up the server on port 8042 and begin an IO loop with it
	server = tornado.httpserver.HTTPServer(application)
	try:
		server.listen(opts.port)
		tornado.ioloop.IOLoop.instance().start()
	except KeyboardInterrupt:
		print
	except socket.error,e:
		print "Socket error:",e
