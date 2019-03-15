import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LinkMatch {
    public GridMatrix gm;
    public GeometryFactory gf = new GeometryFactory();
    Map<String, Double> overall_max_min_lon_lat;
    Map<Point, ArrayList<Map<String, Geometry>>> node_link_relation;

    public LinkMatch(double grid_width) throws IOException {
        // read and parse shp file
        ShpParser SP = new ShpParser();
        overall_max_min_lon_lat = SP.parse_shp_file("./sz_shp_mars/link/shenzhen_mars.shp");
        System.out.println(overall_max_min_lon_lat.get("max_lon") + ", " + overall_max_min_lon_lat.get("max_lat"));
        System.out.println(overall_max_min_lon_lat.get("min_lon") + ", " + overall_max_min_lon_lat.get("min_lat"));
        this.node_link_relation = SP.getNodeLinkRelation();
        // init grid matrix
        this.init_grid_matrix(grid_width);
    }


    public void init_grid_matrix(double grid_width) {
        double max_lon = this.overall_max_min_lon_lat.get("max_lon") + 0.000000000001;
        double max_lat = this.overall_max_min_lon_lat.get("max_lat") + 0.000000000001;
        double min_lon = this.overall_max_min_lon_lat.get("min_lon") - 0.000000000001;
        double min_lat = this.overall_max_min_lon_lat.get("min_lat") - 0.000000000001;
        double n_grids_width = Math.ceil((max_lon - min_lon)/grid_width);
        double n_grids_height = Math.ceil((max_lat - min_lat)/grid_width);
        System.out.println("Init matrix with width * height of " + n_grids_width + " * " + n_grids_height + "...");
        Map<String, Double> matrix_bottom_corner = new HashMap<>();
        matrix_bottom_corner.put("x", min_lon);
        matrix_bottom_corner.put("y", min_lat);
        this.gm = new GridMatrix(matrix_bottom_corner, (int)n_grids_width, (int)n_grids_height, grid_width);
        this.match_links_to_grids();
    }

    public void match_links_to_grids() {
        System.out.println("Processing grid-link match");
        // grids nodes match: iter through every node, see which grid it belongs to
        // add all the links connected with the node to node belonging grid
        for (Map.Entry<Point, ArrayList<Map<String, Geometry>>> node_links : this.node_link_relation.entrySet()) {
            Point node = node_links.getKey();
            ArrayList<Map<String, Geometry>> connected_links = node_links.getValue();
            // node's belonging grid's x and y index
            int grid_index_x = (int)Math.floor((node.getX() - this.gm.bottom_left_x)/this.gm.grid_width);
            int grid_index_y = (int)Math.floor((node.getY() - this.gm.bottom_left_y)/this.gm.grid_width);
            Grid node_belonging_grid = gm.matrix.get(grid_index_x).get(grid_index_y);
            // add links connected with this node to the grid
            for (Map<String, Geometry> link : connected_links) {
                if (!node_belonging_grid.contained_links.contains(link)) {
                    node_belonging_grid.contained_links.add(link);
                }
            }
        }
        // see each grid's contained links
//        for (ArrayList<Grid> col : this.gm.matrix) {
//            for (Grid g : col) {
//                System.out.println(g.contained_links);
//            }
//        }
    }

    public String find_nearest_link(double lon, double lat) {
        // find belonging grid index
        int grid_index_x = (int)Math.floor((lon - this.gm.bottom_left_x)/this.gm.grid_width);
        int grid_index_y = (int)Math.floor((lat - this.gm.bottom_left_y)/this.gm.grid_width);
        Grid belonging_grid;
        // get its neighbours to target grids
        ArrayList<Grid> target_grids = new ArrayList<>();
        try {
            belonging_grid = this.gm.matrix.get(grid_index_x).get(grid_index_y);
            target_grids.add(belonging_grid);
            for (Map.Entry<String, Grid> positon_grid : belonging_grid.neighbours.entrySet()) {
                target_grids.add(positon_grid.getValue());
            }
        } catch (IndexOutOfBoundsException e) {
            //System.out.println("point not in the gird matrix for the shp file!");
            return "-1";
        }
        // iter through each grid's containing links to find the nearest
        Point target_point = this.gf.createPoint(new Coordinate(lon, lat));
        Map<String, Object> nearest_link = new HashMap<>();
        nearest_link.put("linkID", "-1");
        nearest_link.put("distance", Double.POSITIVE_INFINITY);
        for (Grid g : target_grids) {
            // if grid contains no links, nearest_link will remain init key and value above
            for (Map<String, Geometry> link : g.contained_links) {
                String linkID = link.keySet().toArray()[0].toString();
                Geometry linkGeom = (Geometry) link.values().toArray()[0];
                double distance = linkGeom.distance(target_point);
                if (distance < (double) nearest_link.get("distance")) {
                    nearest_link.put("distance", distance);
                    nearest_link.put("linkID", linkID);
                }
            }
        }
        return nearest_link.get("linkID").toString();
    }

    public static void main(String[] args) throws IOException {
        LinkMatch LM = new LinkMatch(0.001);
        System.out.println(LM.find_nearest_link(113.983012,22.550593));
    }

}

