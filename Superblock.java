
public class Superblock {
    private final int defaultInodeBlocks = 64;
    public int totalBlocks;
    public int inodeBlocks;
    public int freeList;
	
	// you implement
	public SuperBlock( int diskSize ) {
		// read the superblock from disk	
		//initialize block byte array
		byte[] superBlock = new byte[Disk.blockSize];
		SysLib.rawread(0, superBlock);
		//assign variables from block 
		totalBlocks = Syslib.bytes2int(superBlock, 0);
		inodeBlocks = SysLib.bytes2int(superBlock, 4);
		freelist = SysLib.bytes2int(superBlock, 8);
		//validate disk block size
		if(totalBlocks != diskSize && inodeBlocks < 1 && freeList < 2){
			totalBlocks = diskSize;
			format(defaultInodeBlocks);
		}
	}
	
	//  helper function
	void sync( ) {
		byte[] superBlock = new byte[Disk.blockSize];
		SysLib.int2bytes( totalBlocks, superBlock, 0 );
		SysLib.int2bytes( inodeBlocks, superBlock, 4 );
		SysLib.int2bytes( freeList, superBlock, 8 );
		SysLib.rawwrite( 0, superBlock );
		SysLib.cerr( "Superblock synchronized\n" );
    }

    void format( ) {
		// default format with 64 inodes
		format( defaultInodeBlocks );
    }
	
	// you implement
	 void format( int files ) {
		// initialize the superblock
		inodeBlocks = files;
		//initialize temp inodes
		for(int i = 0; i < inodeBlocks; i++){
			Inode tempInode = new Inode();
			tempInode.toDisk(i);
		}
		//create freeList size
		freeList = 2 + inodeBlocks * 32 / Disk.blockSize;

		//for every block in freelist, create a new block and then empty it
		for(int i = freeList; i < totalBlocks; i++){
			byte[] tempBlock = new byte[Disk.blockSize];
			for(int j = 0; j < Disk.blockSize; j++)
				tempBlock[j] = 0;
			
			SysLib.int2bytes(i + 1, tempBlock, 0);
			SysLib.rawwrite(i, tempBlock);
		}
		//once format is finished, call sync helper method
		sync();
	 }
	
	// you implement
	public int getFreeBlock( ) {
		// get a new free block from the freelist
		if(freeList > 0 && freeList < totalBlocks){
			//retrive free block
			byte[] temp = new byte[Disk.blockSize];
			SysLib.rawread(freeList, temp);
			
			int dummy = freeList;
			//update next free block
			freeList = SysLib.bytes2int(temp, 0);
			SysLib.int2bytes(0, temp, 0);
			Sys.rawwrite(dummy, temp);
			return dummy;
		}

		return -1;
	}
	
	// you implement
	public boolean returnBlock( int oldBlockNumber ) {
	// return this former block

		//if blockNumber is less than 0, return false
		if(blockNumber < 0)
			return false;
		
		//create a temp data block
		byte tempData[] = new byte[Disk.blockSize];
		for(int i = 0; i < Disk.blockSize; i++)
			tempData[i] = 0;
		
		//implement temp data block and update freeList
		SysLib.rawwrite(blockNumber, tempData);
		SysLib.int2bytes(freeList, tempData, 0);
		freeList = blockNumber;

		return true;
	}
	
}
