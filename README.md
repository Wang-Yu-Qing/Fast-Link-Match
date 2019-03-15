# Link match using GridMatrix
## Idea
The basic idea is to divide the links with **grids** in a **GridMatrix**. Some grids contain links while some don't. The smaller the grid's width is , the fewer links each grid contains on average. We can use the max&min lat lon in the shp file to determine the coordinates of matrix's four corners. Let's say the **bottom left corner** of the matrix is **(lon_min, lat_min)**. The width of each grid is **w**. For a given **(lon, lat)** point, we can calculate its belonging grid's index **i** (index starts from 0) along longitude direction: **i = floor((lon - lon_min)/w)** and index **j** along latitude direction: **j = floor((lon - lat_min)/w)**. So we can locate the point's belonging grid directly. Once we find the grid it belongs to, we can just iter through links contained in that grid and its neighbour grids to find the nearest one. The number of links maybe very small when the grid width is set to a small number.

## Core Classes
### Grid
#### class members
**Grid corners**: Represent four corners' point of the grid.
**Grid polygon**: Represent the polygon geometry of the grid.
**Grid neighbours**: Store the neighbour grids around the grid.
**Links that contained in the grid**: Store the links/link that are contained in the grid.

### GridMatrix
#### class members
**GridWidth**: Width of each grid.
**Matrix**: Store each grid in the matrix col-wise.`[[(1, 1), (1, 2), (1, 3), ...], [(2, 1), (2, 2), (2, 3), ...], ...]`
**Bottom left point**: Represent the coordinate of the matrix's bottom left corner.

## Core Functions
**ShpParser.parse_shp_file(file_path)**: 
1. Read the shp file, get the geometry features of each links
2. Get the overall max lon, max lat, min lon and min lat to determine the matrix corners.
3. Iter through each link's nodes and form the **node-link relation**. `{NodePoint:[{LinkID:LinkGeometry}, ...], ...}`. This reperesents each node's connected links. We will use **node-link relation** to get each grid's contianing links.

**LinkMatch.match_links_to_grids()**:
1. Iter through **node-link relation**, for each node, find out its belonging grid, and add all its connected links to this grid's containing links.

**LinkMatch.find_nearest_link(lon, lat)**:
1. Find the target point's belonging grid.
2. Add this grid and its neighbours to the target grids list.
3. iter through each target grid's containing links to find the nearest link.
