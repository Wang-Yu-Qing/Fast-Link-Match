import com.vividsolutions.jts.geom.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Grid {
    // corner points for the grid
    public Coordinate top_left;
    public Coordinate top_right;
    public Coordinate bottom_right;
    public Coordinate bottom_left;
    // polygon (square) for the grid
    public GeometryFactory gf = new GeometryFactory();
    public Polygon grid_poly;
    public HashMap<String, Grid> neighbours = new HashMap<>();
    public ArrayList<Map<String, Geometry>> contained_links = new ArrayList<>();



    public Grid(Double bottom_left_lon, Double bottom_left_lat, Double width) {
        this.top_left = new Coordinate(bottom_left_lon, bottom_left_lat + width);
        this.top_right = new Coordinate(bottom_left_lon + width, bottom_left_lat + width);
        this.bottom_right = new Coordinate(bottom_left_lon + width, bottom_left_lat);
        this.bottom_left = new Coordinate(bottom_left_lon, bottom_left_lat);
        Coordinate[] corners = {this.top_left, this.top_right, this.bottom_right, this.bottom_left, this.top_left};
        this.grid_poly = gf.createPolygon(corners);
        //System.out.println(this.grid_poly);
    }






    public static void main(String[] args) {
        Grid g = new Grid(0.0, 0.0, 10.0);
    }

}
