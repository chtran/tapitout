package com.example.android.beam;
public class Pair<A, B>
{
    private A first;
    private B second;

    public Pair(A first, B second)
    {
    	super();
    	this.first = first;
    	this.second = second;
    }
    
    public A first()
    {
    	return first;
    }
    
    public B second()
    {
    	return second;
    }
}