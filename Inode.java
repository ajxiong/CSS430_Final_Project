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

	//sets index for block
	public boolean setIndexBlock(short indexNumber){
		//validate contents of direct[]
		for(int i = 0; i < directSize; i++){
			if(direct[i] == -1)
				return false;
		}

		if(indirect != -1)
			return false;
		
		indirect = indexNumber;
		byte[] data = new byte[Disk.blockSize];

		for(int i = 0; i < (Disk.blockSize/ 2); i++)
			SysLib.short2bytes((short) -1, data, i * 2);
		
		SysLib.rawwrite(blockNumber, data);
		return true;
	}

	//find target block 
	public int findTargetBlock(int targetNum){
		//initialize targetBlock
		int targetBlock = targetNum / Disk.blockSize;
		//if targetBlock is less than directSize, return direct index of targetBlock
		if(targetBlock < directSize)
			return direct[targetBlock];
		
		//if indirect is less than 0, return -1
		if(indirect < 0)
			return -1;
		
		//otherwise, indirect has a block
		byte[] data = new byte[Disk.blockSize];
		SysLib.rawread(indirect, data);

		int offset = (targetBlock - directSize) * 2;
		return SysLib.bytes2short(data, offset);
	}
}
