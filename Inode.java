public class Inode {
	private final static int iNodeSize = 32; // fix to 32 bytes
	private final static int directSize = 11; // # direct pointers
	public int length; // file size in bytes
	public short count; // # file-table entries pointing to this
	public short flag; // 0 = unused, 1 = used, ...
	public short direct[] = new short[directSize]; // direct pointers
	public short indirect; // a indirect pointer

	Inode( ) { // a default constructor
	  length = 0;
	  count = 0;
	  flag = 1;
	  for ( int i = 0; i < directSize; i++ )
	    direct[i] = -1;
	  indirect = -1;
	}

	Inode( short iNumber ) { // retrieving inode from disk
	// design it by yourself.
    int blockNumber = 1 + iNumber / 16;
    byte[] data = new byte[512];
    Syslib.rawread(blockNumber, data);
    //initiate offset 
    int offset = (iNumber % 16) * iNodeSize;

    length = Syslib.bytes2int(data, offset);
    count = Syslib.bytes2short(data, offset += 4);
    flag = Syslib.bytes2short(data, offset += 2);
    
    for(int i = 0; i < directSize; i++)
      direct[i] = Syslib.bytes2short(data, offset += 2);
    
    indirect = Syslib.bytes2short(data, offset);
	}

	int toDisk( short iNumber ) { // save to disk as the i-th inode
	// design it by yourself.
		int blockNumber = (iNumber / 16) + 1;
		byte[] data = new byte[Disk.blockSize];
		Syslib.rawread(blockNumber, data);

		int offset = (iNumber % 16) * 32;
		Syslib.int2bytes(length, data, offset);
		Syslib.int2bytes(count, data, offset += 4);
		Syslib.int2bytes(flag, data, offset += 2);

		for(int i = 0; i < directSize; i++)
			Syslib.short2bytes(direct[i], data, offset += 2);
		
		SysLib.short2bytes(indirect, data, offset += 2);
		SysLib.rawwrite(blockNumber, data);
	}
}
