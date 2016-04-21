package com.cisco.cmxmobile.cacheService.client;

public class CachePersistenceException extends Exception 
{

    /**
     * 
     */
    private static final long serialVersionUID = -7488077042962323812L;

    public CachePersistenceException(Throwable e)
    {
	super(e);
    }
    
    public CachePersistenceException(String reason)
    {
	super(reason);
    }
    
    public CachePersistenceException(String reason, Throwable e)
    {
	super(reason, e);
    }

}
