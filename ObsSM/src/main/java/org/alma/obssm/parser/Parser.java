/*******************************************************************************
 * ALMA - Atacama Large Millimeter Array
 * Copyright (c) AUI - Associated Universities Inc., 2016
 * (in the framework of the ALMA collaboration).
 * All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
 *******************************************************************************/

package org.alma.obssm.parser;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

/**
 * This class parse the log with the transitions constrains on the JSON file.
 * @author Javier Fuentes
 * @version 0.3
 */
public class Parser {
    protected Map<String, TransitionConstraints> constraints;
    
    
    /**
     * Constructor, initialize the JSON constraints file.
     *  
     * @param url json file
     * @throws FileNotFoundException
     */
    public Parser(String url) throws FileNotFoundException
    {
        JsonReader reader = new JsonReader(new FileReader(url));
        JsonElement element = new JsonParser().parse(reader);
        Gson gson = new GsonBuilder().create();
        
        this.constraints = new HashMap<>();
        
        JsonArray arr = element.getAsJsonArray();
        
        for (int i=0; i < arr.size(); i++){
            TransitionConstraints t = gson.fromJson(arr.get(i), TransitionConstraints.class);
            this.constraints.put(t.stateName, t);
        }
    }
    
    
    public Map<String, TransitionConstraints> getConstraints() {
        return constraints;
    }



	/**
     * Read a log line and a transition, search for coincidences with the transitions constrains.
     * @param line
     * @param transition
     * @return true when the log line corresponds to transition.
     * @throws NullPointerException
     */
    private boolean parseLine(String line, String transition) throws NullPointerException
    {
        
        TransitionConstraints st = getTransitionConstraints(transition);
        
        if (!st.and_list.stream().noneMatch((aux) -> (!line.contains(aux)))) {
            return false;
        }
        
        if (st.or_list.stream().anyMatch((aux) -> (line.contains(aux)))) {
            return true;
        }
        
        return st.or_list.isEmpty();
    }
    
    
    /**
     * Compares all the transitions possibilities with the log line.
     * @param line
     * @param transitionList
     * @return the corresponding transition, and null when there is no transition.
     */
    public String getParseAction(String line, List<String> transitionList)
    {
        for (String t: transitionList)
        {
            if (parseLine(line, t)) return t;
        }
        
        return null;
    }
    
    
    /**
     * Returns transitions constrains.
     * @param transition
     * @return a transitionconstraint
     * @throws NullPointerException
     */
    private TransitionConstraints getTransitionConstraints(String transition) throws NullPointerException
    {
    	if (transition == null) return null;
        TransitionConstraints st = this.constraints.get(transition);
        if (st == null)
        {
            throw new NullPointerException("The transition do not exists on the HashMap");
        }
        return st;
    }
    
    /**
     * Returns a list with all regexpr matchings fields.
     * @param line
     * @param transition
     * @return
     * @throws NullPointerException
     */
    public List<String> getListSearchPattern(String line, String transition) throws NullPointerException
    {
    	System.out.println(transition);
        TransitionConstraints st = getTransitionConstraints(transition);
        List<String> list =   new ArrayList<>();

        /**
         * Complicated conversion by Netbeans.
         */
        st.search_list.stream().map((pattern) -> Pattern.compile(pattern)).map((p) -> p.matcher(line)).map((m) -> {
            String output = "";
            if (m.find())
            {
                output = m.group();
                System.out.println(output);
            }
            return output;
        }).forEach((output) -> {
            list.add(output);
        });
        
        return list;
    }
    
    /**
     * Returns the keyName found on the line.
     * @param line
     * @param transition
     * @return the keyName or null if not exists.
     */
    public String getKeyName(String line, String transition)
    {
    	if (transition == null) return null;
    	String keyName = null;
    	TransitionConstraints tc = getTransitionConstraints(transition);
    	Pattern pattern = Pattern.compile(tc.keyName);
    	Matcher m = pattern.matcher(line);
    	if (m.find())
    	{
    		keyName = m.group();
    	}
    	return keyName;
    }
}
