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

#XML parser library
from xml.dom import minidom



def getText(dom,name):
	return dom.getElementsByTagName(name)[0].childNodes[0].wholeText


class MainHandler(digest.DigestAuthMixin, tornado.web.RequestHandler):
	def getcreds(uname):
		if uname in MainHandler.creds:
			return MainHandler.creds[uname]
		
		
	#Handle Get Request
	@digest.digest_auth('Dbox',getcreds)
	def get(self,resource):
		realpath = os.path.realpath(os.path.join(self.WEBROOT, resource))
		#Ensure that the requested path (canonicalized) is actually in the user's home directory 
		userdir =  os.path.realpath(os.path.join(self.WEBROOT, self.params['username']))
		if not realpath.startswith(userdir):
			raise tornado.web.HTTPError(403,"Forbidden")
		if not os.path.exists(realpath):
			raise tornado.web.HTTPError(404)
		if os.path.isdir(realpath):
			self.output_directory(resource,realpath)
		elif os.path.isfile(realpath):
			self.output_file(resource,realpath)
			
				
	#Handle Delete Request
	@digest.digest_auth('Dbox',getcreds)
	def delete(self,resource):
		realpath = os.path.realpath(os.path.join(self.WEBROOT, resource))
		userdir =  os.path.realpath(os.path.join(self.WEBROOT, self.params['username']))
		if not realpath.startswith(userdir):
			raise tornado.web.HTTPError(403,"Forbidden")
		if not os.path.exists(realpath):
			raise tornado.web.HTTPError(404,"File or directory not found")
		elif os.path.isdir(realpath):
			shutil.rmtree(realpath)
			self.write("Success: Removed the directory")
		elif os.path.isfile(realpath):
			os.remove(realpath)
			self.write("Success: Removed the file")

			
	#Handle Put Request
	@digest.digest_auth('Dbox',getcreds)
	def put(self, resource):
		dom= minidom.parseString(self.request.body)
		resourceName=getText(dom,"ResourceName")
		resourceLocation=getText(dom,"ResourceLocation")
		resourceCategory=dom.getElementsByTagName('Resource')[0].getAttribute('category')

		realpath = os.path.realpath(os.path.join(self.WEBROOT, resourceLocation, resourceName))
		realdirectory = os.path.realpath(os.path.join(self.WEBROOT, resourceLocation)) 
		userdir = os.path.realpath(os.path.join(self.WEBROOT, self.params['username']))
		

		
		if not realpath.startswith(userdir):
			raise tornado.web.HTTPError(403,"Fobidden")
		if not os.path.isdir(realdirectory):
			raise tornado.web.HTTPError(404,"Directory not found")
		if(resourceCategory == "file"):
			f= open(realpath,"w")
			resourceContent=getText(dom,"ResourceContent")
			resourceEncoding=getText(dom,"ResourceEncoding")
			
			if(resourceEncoding == "Base64"):
				resourceContent=base64.b64decode(resourceContent)
				f.write(resourceContent)
				f.close()
		elif(resourceCategory == "directory"):
			if os.path.exists(realpath):
				raise tornado.web.HTTPError(400,"Bad Request: Directory already exists")
			else:
				os.mkdir(realpath)
		else:
			raise tornado.web.HTTPError(400,"Must be a file or a directory")


	#Output a directory entry (resource list)
	def output_directory(self,resource,realpath):
		self.write("<ResourceList>\n")
		entries = os.listdir(realpath)
		for e in entries:
			epath = os.path.join(realpath, e)
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
			if resource.endswith("/"):
				self.write("\t<ResourceURL>http://%s</ResourceURL>\n" % (self.request.host + "/" + resource + e))
			else:
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
		t = time.localtime(stats.st_mtime)
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
		self.write("\t<ResourceName>%s</ResourceName>\n" % resource.split('/')[-1])
		self.write("\t<ResourceSize>%i</ResourceSize>\n" % stats.st_size)
		#TIME
		self.write("\t<ResourceDate>\n")
		self.write("\t\t<year>%i</year>\n" % t.tm_year)
		self.write("\t\t<month>%i</month>\n" % t.tm_mon)
		self.write("\t\t<day>%i</day>\n" % t.tm_mday)
		self.write("\t\t<hour>%i</hour>\n" % t.tm_hour)
		self.write("\t\t<min>%i</min>\n" % t.tm_min)
		self.write("\t\t<sec>%i</sec>\n" % t.tm_sec)
		self.write("\t</ResourceDate>\n")
		#END TIME
		self.write("\t<ResourceType>%s</ResourceType>\n" % mime)
		self.write("\t<ResourceEncoding>%s</ResourceEncoding>\n" % encoding)
		self.write("\t<ResourceContent>%s</ResourceContent>\n" % encoded_data)
		self.write("</Resource>\n")
		self.write("</ResourceDownload>\n")
	




#Read the password file and store in the MainHandler class.
#Each line of the password file has the format user:password
def read_passwordfile():
	filename=os.path.join(MainHandler.WEBROOT,".passwd")
	creds = {}
	if not os.path.isfile(filename):
		raise Exception("Error: Password file '%s' not found."%filename)
	f = open(filename)
	for l in f:
		if l.find(':') >= 0:
			[user,pw] = l.strip().split(":",1)
			creds[user] = {'auth_username': user, 'auth_password': pw}
	if not creds:
		raise Exception("Error: Password file contained no users (expected a file with lines in the format user:password)")
	MainHandler.creds = creds
	MainHandler.pwpath = os.path.realpath(filename)
