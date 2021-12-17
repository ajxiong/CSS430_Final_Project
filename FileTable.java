import java.util.Vector;

public class FileTable {
	private Vector<E> table; // the actual entity of this file table
	private Directory dir; // the root directory 
	public FileTable( Directory directory ) { // constructor
	table = new Vector( ); // instantiate a file (structure) table
	dir = directory; // receive a reference to the Director
	} // from the file system
	// major public methods
	public synchronized FileTableEntry falloc( String filename, String mode ) {
	// allocate a new file (structure) table entry for this file name
	// allocate/retrieve and register the corresponding inode using dir
	// increment this inode's count
	// immediately write back this inode to the disk
	// return a reference to this file (structure) table entry

		short inum;
		Inode inode = null;

		while (true) {

			//if the file name is the root, found inum
			if(fileName.equals("/")) {
				inum = 0;
			}else {
				//if not the room, find the name using the namei method
				inum = this.dir.namei(fileName);
			}

			//if inum exists
			if(inum >= 0 ) {
				inode = new Inode(inum);


				//if current mode is not read ->write
				if(!mode.equals("r")) {
                    //can wait method if flag is read (2) or write (3)
                    if(inode.flag == 2 || inode.flag == 3)
                    {
                        try { wait(); }//wait for notify
                        catch (InterruptedException e) {}

                    //if flag not read or write, make it write
                    } else {
                        inode.flag = 3;
                        break;//break while loop
                    }
                   
                //otherwise current mode is not write ->read
                }else {
					if(inode.flag == 2) {//if flag is already read, break loop, nothing to do
					break;
					}else if(inode.flag == 3) {//if current flag is write instead of read, just wait
						try { wait(); }//wait for notify
                        catch (InterruptedException e) {}


					}else {//if current flag is anything else, just make flag read and break
						inode.lag = 2;
						break;

					}



				}
			






			}else {//inum file doesnt exist, create it

				//if mode does not equal to "r", create the file
				if (!mode.equals("r")) {
                    //Allocate the file 
                    inum = dir.ialloc(fileName);

                    //Create the iNode and set flag to write (in this case its when flag equals 3)
                    inode = new Inode();
                    inode.flag = 3;
                }
				//break the while loop
                break;




			}

			//if inode still doesnt equal anything, just return null
			if(inode == null) {
				return null;
			}

			inode.count++;
			inode.toDisk(inum);

			FileTableEntry tempEntry = new FileTableEntry(inode, inum,mode);//create new filetable entry
			table.addElement(tempEntry);//add it to table vector
			return tempEntry;


		}
	}
	public synchronized boolean ffree( FileTableEntry e ) {
	// receive a file table entry reference
	// save the corresponding inode to the disk
	// free this file table entry.
	// return true if this file table entry found in my table
	Inode inode = new Inode(e.inum);

	//if remove is successful
	if(table.removeElement(e)) {

		//if count is zero, entry is being unused (unused is zero)
		if(entry.inode.count == 0) {
			entry.inode.flag = 0;


		}


		entry.inode.toDisk(entry.inum);//write to disk
		notifyAll();//notify all to any processes waiting for notify
		return true;
	}//if remove not succesfsful return false
	return false;

	}
	public synchronized boolean fempty( ) {
		return table.isEmpty( ); // return if table is empty 
	} // should be called before starting a format
}
   