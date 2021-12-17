public class FileSystem {
    private SuperBlock superblock;
    private Directory directory;
    private FileTable filetable;

    public FileSystem( int diskBlocks ) {
        // create superblock, and format disk with 64 inodes in default
        superblock = new SuperBlock( diskBlocks );
    
        // create directory, and register "/" in directory entry 0
        directory = new Directory( superblock.inodeBlocks );
    
        // file table is created, and store directory in the file table
        filetable = new FileTable( directory );
    
        // directory reconstruction
        FileTableEntry dirEnt = open( "/", "r" );
        int dirSize = fsize( dirEnt );
        if ( dirSize > 0 ) {
            byte[] dirData = new byte[dirSize];
            read( dirEnt, dirData );
            directory.bytes2directory( dirData );
        }
        close( dirEnt );
    }

    void sync( ) {
        // directory synchronizatioin
        FileTableEntry dirEnt = open( "/", "w" );
        byte[] dirData = directory.directory2bytes( );
        write( dirEnt, dirData );
        close( dirEnt );
    
        // superblock synchronization
        superblock.sync( );
    }

    boolean format( int files ) {
        // wait until all filetable entries are destructed
        while ( filetable.fempty( ) == false )
            ;
    
        // format superblock, initialize inodes, and create a free list
        superblock.format( files );
    
        // create directory, and register "/" in directory entry 0
        directory = new Directory( superblock.inodeBlocks );
    
        // file table is created, and store directory in the file table
        filetable = new FileTable( directory );
    
        return true;
    }

    FileTableEntry open( String filename, String mode ) {
        // filetable entry is allocated
        //create new fileTable entry 
        FileTableEntry newFTableEntry = FileTable.falloc(filename, mode);
        //if mode is "w" AND all blocks are unallocated, then return null
        if(mode.equals("w") && !this.deallocAllBlocks(newFTableEntry))
            return null;
        //otherwise, return newFTableEntry
        return newFTableEntry;
    }

    boolean close( FileTableEntry ftEnt ) {
        // filetable entry is freed
        synchronized ( ftEnt ) {
            // need to decrement count; also: changing > 1 to > 0 below
            ftEnt.count--;
            if ( ftEnt.count > 0 ) // my children or parent are(is) using it
                return true;
        }
        return filetable.ffree( ftEnt );
    }
	
	

    int fsize( FileTableEntry ftEnt ) {
        //cast synchronized to get size of file in bytes
        synchronized(ftEnt){
            return ftEnt.inode.length;
        }
    }


    int read( FileTableEntry ftEnt, byte[] buffer ) {
        if ( ftEnt.mode == "w" || ftEnt.mode == "a" )
            return -1;
    
        int offset   = 0;              // buffer offset
        int left     = buffer.length;  // the remaining data of this buffer
    
        synchronized ( ftEnt ) {
			// repeat reading until no more data  or reaching EOF
            while(ftEnt.seekPtr < fsize(ftEnt) && left > offset){
                //determine target block 
                int targetBlock = entry.inode.findTargetBlock(ftEnt.seekPtr);

                if(targetBlock == -1)
                    break;
                
                //create new read buffer to read into it
                byte[] data = new byte[Disk.blockSize];
                SysLib.rawread(targetBlock, data);

                //initialize data offset
                int dataOffset = ftEnt.seekPtr % Disk.blockSize;

                //initialize remaining bytes, block bytes, and files left to read
                int remainingBytes = buffer.length - left; 
                int remainingBlockBytes = Disk.blockSize - dataOffset;
                int filesLeft = fsize(entry) - ftEnt.seekPtr;
                
                //determine minimum size left to read
                int minSize = Math.min(remainingBytes, Math.min(remainingBlockBytes, filesLeft));
                
                //copy contents of data to buffer
                System.arraycopy(data, dataOffset, buffer, offset, minSize);
                
                //update buffer variables
                offset += minSize;
                ftEnt.seekPtr += minSize;

            }

            return offset;

        }
    }

    int write( FileTableEntry ftEnt, byte[] buffer ) {
        // at this point, ftEnt is only the one to modify the inode
        if ( ftEnt.mode == "r" )
            return -1;
    
        synchronized ( ftEnt ) {
            int offset   = 0;              // buffer offset
            int left     = buffer.length;  // the remaining data of this buffer
            
            while(offset < left)){//change to zero or offset?
                //determine target block
                int targetBlock = ftEnt.inode.findTargetBlock(ftEnt.seekPtr);
                //if targetblock returns invalid
                if(targetBlock == -1){
                    //determine next free block
                    short newBlock = (short) superBlock.getFreeBlock();
                    targetBlock = ftEnt.inode.setTargetBlock(ftEnt.seekPtr, newBlock);

                    switch(targetBlock){
                        case -1:
                        case -2: return -1;
                        case -3{
                            if(!ftEnt.inode.setIndexBlock(short)superBlock.getFreeBlock() || ftEnt.inode.setTargetBlock(ftEnt.seekPtr, newBlock) != 0)
                                return -1;
                        }
                        break;
                    }
                }
                //read targetBlock to a temporary buffer
                byte[] tempBuff = new Byte[Disk.blockSize];
                SysLib.rawread(targetBlock, tempBuff);
                //determine block offset
                short blckOffset = (short) (ftEnt.seekPtr % Disk.BlockSize);
                //determine amount of bytes left
                int remainingBytes = buffer.length - offset;
                int availableBytes = Disk.blockSize - blckOffset;

                int minBytes = Math.min(remainingBytes, availableBytes);
                //copy array to buffer and write it to block
                System.arraycopy(buffer, offset, tempBuff, blckOffset, minBytes);
                SysLib.rawwrite(targetBlock, tempBuff);

                //update variables
                offset += minBytes;
                ftEnt.seekPtr += minBytes;
                //update length of inode
                if(ftEnt.seekPtr > ftEnt.inode.length)
                    ftEnt.inode.length = ftEnt.seekPtr;
            }
            //write to the disk
            ftEnt.inode.toDisk(ftEnt.iNumber);
            return offset;
        }
    }

    private boolean deallocAllBlocks( FileTableEntry ftEnt ) {

        return true;
    }

	
	
	
    boolean delete( String filename ) {
        FileTableEntry ftEnt = open( filename, "w" );
        short iNumber = ftEnt.iNumber;
        return close( ftEnt ) && directory.ifree( iNumber );
    }

    private final int SEEK_SET = 0;
    private final int SEEK_CUR = 1;
    private final int SEEK_END = 2;

    int seek( FileTableEntry ftEnt, int offset, int whence ) {
        synchronized ( ftEnt ) {
            /*
            System.out.println( "seek: offset=" + offset +
                    " fsize=" + fsize( ftEnt ) +
                    " seekptr=" + ftEnt.seekPtr +
                    " whence=" + whence );
            */
			int size = fsize(entry);
            
            switch(whence){
                case 0:{
                    if(offset >= 0 && offset <= size){
                        ftEnt.seekptr = offset;
                        break;
                    }
                    return -1;
                }
                case 1:{
                    if(ftEnt.seekPtr + offset >= 0 && ftEnt.seekPtr + offset <= size){
                        ftEnt.seekPtr += offset;
                        break;
                    }
                    return -1;
                }
                case 2:{
                    ftEnt.seekPtr = size + offset;
                    break;
                }
            }
		}
        return ftEnt.seekPtr;
    }
}
