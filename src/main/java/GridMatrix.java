
import java.util.ArrayList;
import java.util.Map;

public class GridMatrix {
    public double grid_width;
    public ArrayList<ArrayList<Grid>> matrix = new ArrayList<>();
    public double bottom_left_x;
    public double bottom_left_y;



    public GridMatrix(Map<String, Double> topleft_corner, int grids_num_x, int grids_num_y, double grid_width) {
        this.grid_width = grid_width;
        bottom_left_x = topleft_corner.get("x");
        bottom_left_y = topleft_corner.get("y");
        // init grids:
        System.out.println(String.format("Matrix has %s x-grids and %s y-grids", grids_num_x, grids_num_y));
        // grid's x, y index start from 0 to num-1
        for (int i = 0; i < grids_num_x; i++) {
            ArrayList<Grid> column = new ArrayList<>();
            for (int j = 0; j < grids_num_y; j++) {
                Grid grid = new Grid(bottom_left_x + i * this.grid_width, bottom_left_y + j * this.grid_width, this.grid_width);
                column.add(grid);
                //System.out.println(i + ", " + j + " -- > " + grid.grid_poly);
            }
            this.matrix.add(column);
        }
        // add neighbours for each grid
        System.out.println("Adding neighbours for each grid...");
        for (int i = 0; i < grids_num_x; i++) {
            for (int j = 0; j < grids_num_y; j++) {
                // add left neighbour
                if (i > 0) {
                    this.matrix.get(i).get(j).neighbours.put("left", this.matrix.get(i - 1).get(j));
                }
                // add right neighbour
                if (i < grids_num_x - 1) {
                    this.matrix.get(i).get(j).neighbours.put("right", this.matrix.get(i + 1).get(j));
                }
                // add up neighbour
                if (j < grids_num_y - 1) {
                    this.matrix.get(i).get(j).neighbours.put("up", this.matrix.get(i).get(j + 1));
                }
                // add down neighbour
                if (j > 0) {
                    this.matrix.get(i).get(j).neighbours.put("down", this.matrix.get(i).get(j - 1));
                }
//                System.out.println("Grid:");
//                System.out.println(this.matrix.get(i).get(j).grid_poly);
//                System.out.println("Neighbours:");
//                for (Map.Entry<String, Grid> k_v: this.matrix.get(i).get(j).neighbours.entrySet()){
//                    System.out.println(k_v.getValue().grid_poly);
            }
        }
    }


}

