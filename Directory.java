public class Directory {
	private static int maxChars = 30; // max characters of each file name
	// Directory entries
	private int fsize[]; // each element stores a different file size.
	private char fnames[][]; // each element stores a different file name.
	public Directory( int maxInumber ) { // directory constructor
	fsize = new int[maxInumber]; // maxInumber = max files
	for ( int i = 0; i < maxInumber; i++ ) 
	fsize[i] = 0; // all file size initialized to 0
	fnames = new char[maxInumber][maxChars];
	String root = "/"; // entry(inode) 0 is "/"
	fsize[0] = root.length( ); // fsize[0] is the size of "/".
	root.getChars( 0, fsize[0], fnames[0], 0 ); // fnames[0] includes "/"
	}

	public int bytes2directory( byte data[] ) {
	// assumes data[] received directory information from disk
	// initializes the Directory instance with this data[]

	//offset used to keep track of position
	int offset = 0;
	//get information from data[]
	for (int i = 0; i < fsize.length; i++)
	{
		fsize[i] = SysLib.bytes2int(data, offset);
		offset += 4;
	}

	// initializes the Directory instance with the data info gained from before
	for (int i = 0; i < fsize.length; i++)
	{
		String tempString = new String(data, offset, 60);
		tempString.getChars(0, fsize[i], fnames[i], 0);
		//offset is increased by 60 because that is the maximum of bytes
		offset += 60;
	}


	}

	public byte[] directory2bytes( ) {
	// converts and return Directory information into a plain byte array
	// this byte array will be written back to disk
	// note: only meaningfull directory information should be converted
	// into bytes.



		byte [] directory = new byte[64 * fsize.length];//create byte array to return later


		int offset = 0;
		for (int i = 0; i < fsize.length; i++)//loop trhough fsize and covert int into byte
        {
            SysLib.int2bytes(fsize[i], directory, offset);
            offset += 4;
        }

		for (int i = 0; i < fsize.length; i++)//loop throuhg fsize and copy it into the directory byte array
        {
            String tempString = new String(fnames[i], 0, fsize[i]);
            byte [] bytes = tempString.getBytes();
            System.arraycopy(bytes, 0, directory, offset, bytes.length);

			//offset is increased by 60 because that is the maximum of bytes
            offset += 60;
        }

		return directory;//return directory 



	}

	public short ialloc( String filename ) {
	// filename is the one of a file to be created.
	// allocates a new inode number for this filename


		//file name from parameter is created from looping from fsize
		for (short i = 0; i < fsize.length; i++)
		{
			if (fsize[i] == 0)
			{
				// created a number for this new file
				int tempFile = filename.length() > maxChars ? maxChars : filename.length();
				fsize[i] = tempFile;
				filename.getChars(0, fsize[i], fnames[i], 0);
				//return true because is found
				return i;
			}
		}
		return -1;
	}

	public boolean ifree( short iNumber ) {
	// deallocates this inumber (inode number)
	// the corresponding file will be deleted.
		if(iNumber < 30 && fsize[iNumber] > 0){      //if iNumber is less than 30 and spot in fsize is greater than zero
			fsize[iNumber] = 0;                  //make it zero          
			return true;                                 //file was found
		} else {
			return false;                                 //file not found
		}
	}

	public short namei( String filename ) {
	// returns the inumber corresponding to this filename

		//loop through fsize
		for(short i = 0; i < fsize.length; i++) {

			//Get the file name, if it matches return its location
			String tempString = new String(fnames[i], 0, fsize[i]);
			if(tempString.equals(filename)){//check if strings are equal
				return i;//return i if found
			}
		}

		return -1;//return -1 if not found




	}
}
   