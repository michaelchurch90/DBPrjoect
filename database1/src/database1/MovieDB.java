
/*******************************************************************************
 * @file  MovieDB.java
 *
 * @author   John Miller
 */

package database1;

import static java.lang.System.out;

/*******************************************************************************
 * The MovieDB class makes a Movie Database.  It serves as a template for making
 * other databases.  See "Database Systems: The Complete Book", second edition,
 * page 26 for more information on the Movie Database schema.
 */

class MovieDB
{
    /***************************************************************************
     * Main method for creating, populating and querying a Movie Database.
     * @param args  the command-line arguments
     */
    public static void main (String [] args)
    {
        //out.println ();

        Table movie = new Table ("movie", "title year length genre studioName producerNo",
                                          "String Integer Integer String String Integer", "title year");  

        Table cinema = new Table ("cinema", "title year length genre studioName producerNo",
                                            "String Integer Integer String String Integer", "title year");

        Table movieStar = new Table ("movieStar", "name address gender birthdate",
                                                  "String String Character String", "name");

        Table starsIn = new Table ("starsIn", "movieTitle movieYear starName", 
                                              "String Integer String", "movieTitle movieYear starName"); //starName

        Table movieExec = new Table ("movieExec", "certNo name address fee",
                                                  "Integer String String Float", "certNo"); //Double

        Table studio = new Table ("studio", "name address presNo",
                                            "String String Integer", "name");

        Comparable [] film0 = { "Star_Wars", 1977, 124, "sciFi", "Fox", 12345 };
        Comparable [] film1 = { "Star_Wars_2", 1980, 124, "sciFi", "Fox", 12345 };
        Comparable [] film2 = { "Rocky", 1985, 200, "action", "Universal", 12125 };
        Comparable [] film3 = { "Rambo", 1978, 100, "action", "Universal", 32355 };
        //out.println ();
        movie.insert (film0);
        movie.insert (film1);
        movie.insert (film2);
        movie.insert (film3);
        //movie.print ();
                

        Comparable [] film4 = { "Galaxy_Quest", 1999, 104, "comedy", "DreamWorks", 67890 };
        //out.println ();
        cinema.insert (film2);
        cinema.insert (film3);
        cinema.insert (film4);
        //cinema.print ();

        Comparable [] star0 = { "Carrie_Fisher", "Hollywood", 'F', "9/9/99" };
        Comparable [] star1 = { "Mark_Hamill", "Brentwood", 'M', "8/8/88" };
        Comparable [] star2 = { "Harrison_Ford", "Beverly_Hills", 'M', "7/7/77" };
        //out.println ();
        movieStar.insert (star0);
        movieStar.insert (star1);
        movieStar.insert (star2);
        //movieStar.print ();

        Comparable [] cast0 = { "Star_Wars", 1977, "Carrie_Fisher" };
        //out.println ();
        starsIn.insert (cast0);
        //starsIn.print ();

        Comparable [] exec0 = { 9999, "S_Spielberg", "Hollywood", 10000.00f };  
        //out.println ();
        movieExec.insert (exec0);
        //movieExec.print ();

        Comparable [] studio0 = { "Fox", "Los_Angeles", 7777 };
        Comparable [] studio1 = { "Universal", "Universal_City", 8888 };
        Comparable [] studio2 = { "DreamWorks", "Universal_City", 9999 };
        //out.println ();
        studio.insert (studio0);
        studio.insert (studio1);
        studio.insert (studio2);
        //studio.print ();
        
       // out.println();
       
        
        Table t_select = movie.select ("title == 'Star_Wars'"); 
        t_select.print();
        
        Table t_select2 = movie.select ("length > 100 & studioName == 'Universal' | genre == 'sciFi'" );  
        t_select2.print();
        
        
        Table t_project = movie.project ("title year");
        t_project.print();
        
        Table t_project2 = cinema.project("title genre studioName");
        t_project2.print();
        
        
        Table t_union = movie.union (cinema);
        t_union.print();
        
        Table t_union2 = movieStar.union (studio);
        t_union2.print();
        
        Table t_minus = movie.minus (cinema);
        t_minus.print();
        
        Table t_minus2 = movieStar.minus (studio);
        t_minus2.print();
        
        
        Table t_join =  movie.join ("studioName == name", studio);
        t_join.print();
        
        Table t_join2 = movieStar.join ("name == starName", starsIn);
        t_join2.print();
        
        
        /*
        out.println ();
//        Table t_project = movie.project ("title year");
        Table t_project = cinema.project("title genre studioName");
        t_project.print ();

        out.println ();
        Table t_select = movie.select ("length > 100 & studioName == 'Universal' | genre == 'sciFi'" );   
//        Table t_select = movieStar.select ("gender == M");
//        Table t_select = movieExec.select("10000 == fee");
//        Table t_select = movie.select ("title == 'Star_Wars_2'");    
        t_select.print ();
        

        out.println ();
//        Table t_union = movie.union (cinema);
        Table t_union = movieStar.union (studio);
        t_union.print ();

        out.println ();
//        Table t_minus = movie.minus (cinema);
        Table t_minus = movieStar.minus (studio);
        t_minus.print ();

        out.println ();
//        Table t_join =  movie.join ("studioName == name", studio);
        Table t_join = movieStar.join ("name == starName", starsIn);
//        Table t_join =  movie.join ("year < c.year & studioName == c.studioName", cinema);
//        Table t_join =  movie.join ("studioName == c.studioName", cinema);
        t_join.print ();
        */
    } // main

} // MovieDB class

