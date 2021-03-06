package com.master;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

public class FileDirLock {
	Hashtable<String, ReadWriteLock> locks;
	
	public FileDirLock(){
		locks = getFilesDirs("");
		locks.put("/", new ReadWriteLock());
	}

	// map all files/dirs to value 0 originally
	private Hashtable<String, ReadWriteLock> getFilesDirs(String directory){
		File Directory = new File("source" + directory);
		String [] contents = Directory.list();
		Hashtable<String, ReadWriteLock> concat = new Hashtable<String, ReadWriteLock>();
		for(int a = 0; a < contents.length; a++){
			concat.put(directory + "/" + contents[a], new ReadWriteLock());
			if(Files.isDirectory(Paths.get("source" + directory + "/" + contents[a])))
				concat.putAll(getFilesDirs(directory + "/" + contents[a]));
		}
		return concat;
	}
	
	public void acquireReadLock(String path) throws InterruptedException{
		Vector<String> dirs = getParentDirs(path);
		for(int a = 0; a < dirs.size()-1; a++){
			locks.get(dirs.get(a)).lockIntentRead();
//			System.out.println("intent read lock: " + dirs.get(a));
		}
		locks.get(dirs.get(dirs.size()-1)).lockRead();
	}
	
	public void acquireWriteLock(String path) throws InterruptedException{
		Vector<String> dirs = getParentDirs(path);
		for(int a = 0; a < dirs.size()-1; a++){
			locks.get(dirs.get(a)).lockIntentWrite();
//			System.out.println("intent write lock: " + dirs.get(a));
		}
//		System.out.println("acquire lock " + dirs.get(dirs.size()-1));
		locks.get(dirs.get(dirs.size()-1)).lockWrite();
	}
	
	public void releaseReadLock(String path) throws InterruptedException{
		Vector<String> dirs = getParentDirs(path);
		for(int a = 0; a < dirs.size()-1; a++){
			locks.get(dirs.get(a)).unlockIntentRead();
		}
		locks.get(dirs.get(dirs.size()-1)).unlockRead();
	}
	
	public void releaseWriteLock(String path) throws InterruptedException{
		Vector<String> dirs = getParentDirs(path);
		for(int a = 0; a < dirs.size()-1; a++){
			locks.get(dirs.get(a)).unlockIntentWrite();
		}
//		System.out.println("release lock " + path);
		locks.get(dirs.get(dirs.size()-1)).unlockWrite();
	}
	
	//returns string vector of parent directory paths
	private Vector<String> getParentDirs(String filePath){
		StringTokenizer st = new StringTokenizer(filePath, "/\\");
		String partial = "";
		Vector<String> parents = new Vector<String>();
		parents.add("/");
		while(st.hasMoreTokens()){
			partial += "/" + st.nextToken();
			parents.add(partial);
		}
		return parents;		
	}
	
	public Vector<String> getChildren(String directory){
		File Directory = new File("source" + directory);
		String [] contents = Directory.list();
		Vector<String> concat = new Vector<String>();
		for(int a = 0; a < contents.length; a++){
			concat.add(directory + "/" + contents[a]);
			if(Files.isDirectory(Paths.get("source" + directory + "/" + contents[a])))
				concat.addAll(getChildren(directory + "/" + contents[a]));
		}
		return concat;
	}
	
	public void createLock(String path){
		if(path.endsWith("/"))
			path = path.substring(0, path.length()-2);
		locks.put(path, new ReadWriteLock());
	}

	
	public void renameLock(String src, String newName) {
		locks.put(newName, locks.get(src));
		Vector<String> children = getChildren(src);
		for(int a = 0 ; a < children.size(); a++){
			if(!Files.isDirectory(Paths.get("source" + children.get(a)))){
				String name = children.get(a);
				name.replace(src, newName);
				locks.put(name, locks.get(src));
			}
		}
		
	}

	private class ReadWriteLock{
		private int readers       = 0;
		private int writers       = 0;
		private int writeRequests = 0;
		private int intentReaders = 0;
		private int intentWriters = 0;
		private int intentWriteRequests = 0;

		public synchronized void lockRead() throws InterruptedException{
			while(writers > 0 || writeRequests > 0 || intentWriters > 0 || intentWriteRequests > 0){
				wait();
			}
			readers++;
		}

		public synchronized void unlockRead(){
			readers--;
			notifyAll();
		}

		public synchronized void lockWrite() throws InterruptedException{
			writeRequests++;

			while(readers > 0 || writers > 0 || intentReaders > 0 || intentWriters > 0){
				wait();
			}
			writeRequests--;
			writers++;
		}

		public synchronized void unlockWrite() throws InterruptedException{
			writers--;
			notifyAll();
		}
		
		public synchronized void lockIntentRead() throws InterruptedException{
			while(writers > 0 || writeRequests > 0){
				wait();
			}
			intentReaders++;
		}
		
		public synchronized void unlockIntentRead(){
			intentReaders--;
			notifyAll();
		}
		
		public synchronized void lockIntentWrite() throws InterruptedException{
			intentWriteRequests++;

			while(readers > 0 || writers > 0 || writeRequests > 0){
				wait();
			}
			intentWriteRequests--;
			intentWriters++;
		}
		
		public synchronized void unlockIntentWrite(){
			intentWriters--;
			notifyAll();
		}
	}


}
