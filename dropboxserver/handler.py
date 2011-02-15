#!/usr/bin/python
import tornado.httpserver
import tornado.web
#Use a third-party implementation of digest auth
from curtain import digest

import os
import time
import mimetypes
import base64
import shutil


class MainHandler(digest.DigestAuthMixin, tornado.web.RequestHandler):
	def getcreds(uname):
		if uname in MainHandler.creds:
			return MainHandler.creds[uname]
		
		
	#Handle Get Request
	@digest.digest_auth('Authusers',getcreds)
	def get(self,resource):
		realpath = os.path.realpath(self.WEBROOT + "/" + resource)
		if not os.path.exists(realpath):
			raise tornado.web.HTTPError(404)
		if self.pwpath == realpath:
			#Forbid password file requests
			raise tornado.web.HTTPError(403,"Forbidden")
		elif os.path.isdir(realpath):
			self.output_directory(resource,realpath)
		elif os.path.isfile(realpath):
			self.output_file(resource,realpath)
			
				
	#Handle Delete Request
	@digest.digest_auth('Authusers',getcreds)
	def delete(self,resource):
		realpath = self.WEBROOT + "/" + resource
		if not os.path.exists(realpath):
			raise tornado.web.HTTPError(404,"File or directory not found")
		elif os.path.isdir(realpath):
			shutil.rmtree(realpath)
			self.write("Success: Removed the directory")
		elif os.path.isfile(realpath):
			os.remove(realpath)
			self.write("Success: Removed the file")
			


	#Output a directory entry (resource list)
	def output_directory(self,resource,realpath):
		self.write("<ResourceList>\n")
		entries = os.listdir(realpath)
		for e in entries:
			epath = realpath + '/' + e
			stats = os.stat(epath);
			t = time.localtime(stats.st_mtime)
			mime = mimetypes.guess_type(epath)[0]
			#Default if mimetype not detected
			if not mime:
				mime = "application/octet-stream"

			if os.path.isdir(epath):
				category = "directory"
			else:
				category = "file"

			self.write("<Resource category=\"%s\">\n" % category)
			self.write("\t<ResourceName>%s</ResourceName>\n" % e)
			self.write("\t<ResourceSize>%i</ResourceSize>\n" % stats.st_size)
			self.write("\t<ResourceURL>http://%s</ResourceURL>\n" % (self.request.host + "/" + resource + "/" + e))
			self.write("\t<ResourceDate>\n")
			self.write("\t\t<year>%i</year>\n" % t.tm_year)
			self.write("\t\t<month>%i</month>\n" % t.tm_mon)
			self.write("\t\t<day>%i</day>\n" % t.tm_mday)
			self.write("\t\t<hour>%i</hour>\n" % t.tm_hour)
			self.write("\t\t<min>%i</min>\n" % t.tm_min)
			self.write("\t\t<sec>%i</sec>\n" % t.tm_sec)
			self.write("\t</ResourceDate>\n")
			self.write("\t<ResourceType>%s</ResourceType>\n" % mime)
			self.write("</Resource>\n")
		self.write("</ResourceList>")

	#Output a file with its contents
	def output_file(self,resource,realpath):
		self.write("<ResourceDownload>\n")
		stats = os.stat(realpath);
		mime = mimetypes.guess_type(realpath)[0]
		if not mime:
			mime = "application/octet-stream"

		data = file(realpath).read()
		if mime == "text/plain":
			encoding = "Text"
			encoded_data = data
		else:
			encoding = "Base64"
			encoded_data = base64.b64encode(data)

		self.write("<Resource category=\"file\">\n")
		self.write("\t<ResourceName>%s</ResourceName>\n" % resource)
		self.write("\t<ResourceSize>%i</ResourceSize>\n" % stats.st_size)
		self.write("\t<ResourceType>%s</ResourceType>\n" % mime)
		self.write("\t<ResourceEncoding>%s</ResourceEncoding>\n" % encoding)
		self.write("\t<ResourceContent>%s</ResourceContent>\n" % encoded_data)
		self.write("</Resource>\n")
		self.write("</ResourceDownload>\n")


#Read the password file and store in the MainHandler class.
#Each line of the password file has the format user:password
def read_passwordfile():
	if True:
		filename=MainHandler.WEBROOT+"/.passwd"
		creds = {}
		f = open(filename)
		for l in f:
			[user,pw] = l.strip().split(":",1)
			creds[user] = {'auth_username': user, 'auth_password': pw}
		MainHandler.creds = creds
		MainHandler.pwpath = os.path.realpath(filename)