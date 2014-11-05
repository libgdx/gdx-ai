/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.badlogic.gdx.ai.tests.pfa.tests.tiled;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;

/** Utility class to generate flat and hierarchical random dungeons.
 * 
 * @author davebaol */
public final class DungeonUtils {

	public static void main (String[] args) {
//		int mapSizeX = 60;
//		int mapSizeY = 100;
//		int map[][] = DungeonUtils.generate(mapSizeX, mapSizeY, MathUtils.random(70, 120), 3, 15, 100);
//		System.out.println(mapToString(map));

		int mapSizeX = 75;
		int mapSizeY = 125;
		int map[][] = new int[mapSizeX][mapSizeY];
		int submapSizeX = mapSizeX / 2;
		int submapSizeY = mapSizeY / 3;
		
		for (int x = 0; x < 2; x++) {
			for (int y = 0; y < 3; y++) {
				int submap[][] = DungeonUtils.generate(submapSizeX, submapSizeY, MathUtils.random(15, 67), 1, 8, 96, false);
				for (int x0 = 0; x0 < submapSizeX; x0++) {
					for (int y0 = 0; y0 < submapSizeY; y0++) {
						if (submap[x0][y0] != TILE_EMPTY)
							map[x*submapSizeX+x0][y*submapSizeY+y0] = submap[x0][y0];
					}
				}
			}
		}

		// Generate corridors to connect buildings
		for (int x = 0; x < 2 ; x++) {
			for (int y = 0; y < 3; y++) {
				// Generates 1 or 2 corridors per building
				boolean corridor1 = y < 3 - 1;
				boolean corridor2 = x < 2 - 1;
				if (corridor1 && corridor2 && MathUtils.randomBoolean(.4f)) {
					if (MathUtils.randomBoolean())
						corridor1 = false;
					else
						corridor2 = false;
				}
				if (corridor1) {
					int x0 = MathUtils.random(x * submapSizeX + 1, x * submapSizeX + submapSizeX / 2);
					int y0 = y * submapSizeY + 1;
					for (int i = submapSizeY + 5; i > 0 ; i--) {
						if (i < submapSizeY && map[x0][y0 + i] == TILE_FLOOR)
							break;
						map[x0][y0 + i] = TILE_FLOOR;
					}
				}
				if (corridor2) {
					int x1 = x * submapSizeX + 1;
					int y1 = MathUtils.random(y * submapSizeY + 1, y * submapSizeY + submapSizeY / 2);
					for (int i = submapSizeX + 5; i > 0 ; i--) {
						if (i < submapSizeX && map[x1 + i][y1] == TILE_FLOOR)
							break;
						map[x1 + i][y1] = TILE_FLOOR;
					}
				}
			}
		}

		addMissingWalls(map);
		System.out.println(mapToString(map));
	}

	static final int TILE_EMPTY = 0;
	static final int TILE_FLOOR = 1;
	static final int TILE_WALL = 2;

	private DungeonUtils () {
	}

	public static int[][] generate (int mapSizeX, int mapSizeY, int roomCount, int roomMinSize, int roomMaxSize, int squashIterations) {
		return generate (mapSizeX, mapSizeY, roomCount, roomMinSize, roomMaxSize, squashIterations, true);
	}

	public static int[][] generate (int mapSizeX, int mapSizeY, int roomCount, int roomMinSize, int roomMaxSize, int squashIterations, boolean addMissingWalls) {
		int[][] map = new int[mapSizeX][mapSizeY];
		for (int x = 0; x < mapSizeX; x++) {
			for (int y = 0; y < mapSizeY; y++) {
				map[x][y] = TILE_EMPTY;
			}
		}

		// Generate random rooms and make sure they don't collide each other.
		// Also decrease the room width and height by 1 so as to make sure that no two rooms
		// are directly next to one another (making one big room).
		Array<Room> rooms = new Array<Room>();
		for (int k = 0; k < squashIterations; k++) {
			System.out.println("k:" + k + ", rooms:" + rooms.size);
			int failures = 0;
			for (int i = rooms.size; i < roomCount; i++) {
				if (failures > 1000) {
					break;
				}

				Room room = new Room();
				room.x = MathUtils.random(1, mapSizeX - roomMaxSize - 1);
				room.y = MathUtils.random(1, mapSizeY - roomMaxSize - 1);
				room.w = MathUtils.random(roomMinSize, roomMaxSize);
				room.h = MathUtils.random(roomMinSize, roomMaxSize);

				if (collides(rooms, room)) {
					i--;
					failures++;
					continue;
				}
				room.w--;
				room.h--;

				rooms.add(room);
			}

			// Move all the rooms closer to one another to get rid of some large gaps
			squashRooms(rooms);
		}
		roomCount = rooms.size;

		// Build corridors between rooms that are near to one another.
		// We choose a random point in each room and then move the second point towards the
		// first one (in the while loop).
		for (int i = 0; i < roomCount; i++) {
			Room roomA = rooms.get(i);
			Room roomB = findClosestRoom(rooms, roomA);

			int pointAx = MathUtils.random(roomA.x, roomA.x + roomA.w);
			int pointAy = MathUtils.random(roomA.y, roomA.y + roomA.h);

			int pointBx = MathUtils.random(roomB.x, roomB.x + roomB.w);
			int pointBy = MathUtils.random(roomB.y, roomB.y + roomB.h);

			while ((pointBx != pointAx) || (pointBy != pointAy)) {
				if (pointBx != pointAx) {
					if (pointBx > pointAx)
						pointBx--;
					else
						pointBx++;
				} else if (pointBy != pointAy) {
					if (pointBy > pointAy)
						pointBy--;
					else
						pointBy++;
				}

				map[pointBx][pointBy] = TILE_FLOOR;
			}
		}

		// Iterate through all the rooms and set the tile to FLOOR for every tile within a room
		for (int i = 0; i < roomCount; i++) {
			Room room = rooms.get(i);
			for (int x = room.x; x < room.x + room.w; x++) {
				for (int y = room.y; y < room.y + room.h; y++) {
					map[x][y] = TILE_FLOOR;
				}
			}
		}

		// Convert to a wall tile any empty tile touching a floor tile
		if (addMissingWalls)
			addMissingWalls(map);

		return map;
	}

	public static TwoLevelHierarchy generate2LevelHierarchy (int mapSizeX, int mapSizeY, int buildingsX, int buildingsY, int roomMinCount, int roomMaxCount, int roomMinSize, int roomMaxSize, int squashIterations) {
		int map[][] = new int[mapSizeX][mapSizeY];
		boolean level1Con1[][] = new boolean[buildingsX][buildingsY];
		boolean level1Con2[][] = new boolean[buildingsX][buildingsY];
		int submapSizeX = mapSizeX / buildingsX;
		int submapSizeY = mapSizeY / buildingsY;
		
		// Generate buildings
		for (int x = 0; x < buildingsX; x++) {
			for (int y = 0; y < buildingsY; y++) {
				int submap[][] = DungeonUtils.generate(submapSizeX, submapSizeY, MathUtils.random(roomMinCount, roomMaxCount), roomMinSize, roomMaxSize, squashIterations, false);
				for (int x0 = 0; x0 < submapSizeX; x0++) {
					for (int y0 = 0; y0 < submapSizeY; y0++) {
						if (submap[x0][y0] != TILE_EMPTY)
							map[x*submapSizeX+x0][y*submapSizeY+y0] = submap[x0][y0];
					}
				}
			}
		}

		// Generate corridors to connect buildings
		for (int x = 0; x < buildingsX ; x++) {
			for (int y = 0; y < buildingsY; y++) {
				// Generates 1 or 2 corridors per building
				boolean corridor1 = y < buildingsY - 1;
				boolean corridor2 = x < buildingsX - 1;
				if (corridor1 && corridor2 && MathUtils.randomBoolean(.4f)) {
					if (MathUtils.randomBoolean())
						corridor1 = false;
					else
						corridor2 = false;
				}
				if (corridor1) {
					level1Con1[x][y] = true;
					int x0 = MathUtils.random(x * submapSizeX + 1, x * submapSizeX + submapSizeX / 2);
					int y0 = y * submapSizeY + 1;
					for (int i = submapSizeY + 5; i > 0 ; i--) {
						if (i < submapSizeY && map[x0][y0 + i] == TILE_FLOOR)
							break;
						map[x0][y0 + i] = TILE_FLOOR;
					}
				}
				if (corridor2) {
					level1Con2[x][y] = true;
					int x1 = x * submapSizeX + 1;
					int y1 = MathUtils.random(y * submapSizeY + 1, y * submapSizeY + submapSizeY / 2);
					for (int i = submapSizeX + 5; i > 0 ; i--) {
						if (i < submapSizeX && map[x1 + i][y1] == TILE_FLOOR)
							break;
						map[x1 + i][y1] = TILE_FLOOR;
					}
				}
			}
		}

		addMissingWalls(map);
//		System.out.println(mapToString(map));
		
		return new TwoLevelHierarchy(map, level1Con1, level1Con2);
	}
	
	// Iterates through all the tiles in the map and if it finds a tile that is a FLOOR
	// we check all the surrounding tiles for empty values. If we find an empty tile (that
	// touches the floor) we build a WALL.
	public static void addMissingWalls(int[][] map) {
		int mapSizeX = map.length;
		int mapSizeY = map[0].length;
		for (int x = 0; x < mapSizeX; x++) {
			for (int y = 0; y < mapSizeY; y++) {
				if (map[x][y] == TILE_FLOOR) {
					for (int xx = x - 1; xx <= x + 1; xx++) {
						for (int yy = y - 1; yy <= y + 1; yy++) {
							if (map[xx][yy] == TILE_EMPTY) map[xx][yy] = TILE_WALL;
						}
					}
				}
			}
		}
	}

	private static boolean collides (Array<Room> rooms, Room room) {
		return collides(rooms, room, -1);
	}

	private static boolean collides (Array<Room> rooms, Room room, int ignore) {
		for (int i = 0; i < rooms.size; i++) {
			if (i == ignore) continue;
			Room check = rooms.get(i);
			if (!((room.x + room.w < check.x) || (room.x > check.x + check.w) || (room.y + room.h < check.y) || (room.y > check.y
				+ check.h))) return true;
		}

		return false;
	}

	private static void squashRooms (Array<Room> rooms) {
		for (int i = 0; i < 20; i++) {
			for (int j = 0; j < rooms.size; j++) {
				Room room = rooms.get(j);
				while (true) {
					int oldX = room.x;
					int oldY = room.y;
					if (room.x > 1) room.x--;
					if (room.y > 1) room.y--;
					if ((room.x == 1) && (room.y == 1)) break;
					if (collides(rooms, room, j)) {
						room.x = oldX;
						room.y = oldY;
						break;
					}
				}
			}
		}
	}

	private static Room findClosestRoom (Array<Room> rooms, Room room) {
		float midX = room.x + (room.w / 2f);
		float midY = room.y + (room.h / 2f);
		Room closest = null;
		float closestDistance = Float.POSITIVE_INFINITY;
		for (int i = 0; i < rooms.size; i++) {
			Room check = rooms.get(i);
			if (check == room) continue;
			float checkMidX = check.x + (check.w / 2f);
			float checkMidY = check.y + (check.h / 2f);
			float distance = Math.min(Math.abs(midX - checkMidX) - (room.w / 2f) - (check.w / 2f), Math.abs(midY - checkMidY)
				- (room.h / 2f) - (check.h / 2f));
			if (distance < closestDistance) {
				closestDistance = distance;
				closest = check;
			}
		}
		return closest;
	}

	public static String mapToString (int[][] map) {
		StringBuilder sb = new StringBuilder(map.length * (map[0].length + 1)); // +1 is due to the new line char
		for (int x = 0; x < map.length; x++) {
			for (int y = 0; y < map[0].length; y++) {
				switch (map[x][y]) {
				case TILE_EMPTY:
					sb.append(' ');
					break;
				case TILE_FLOOR:
					sb.append('.');
					break;
				case TILE_WALL:
					sb.append('#');
					break;
				default:
					sb.append('?');
					break;
				}
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	private static class Room {
		int x, y, w, h;
	}

	public static class TwoLevelHierarchy {
		public int[][] level0;
		public boolean[][] level1Con1;
		public boolean[][] level1Con2;
		public TwoLevelHierarchy(int[][] level0, boolean[][] level1Con1, boolean[][] level1Con2) {
			this.level0 = level0;
			this.level1Con1 = level1Con1;
			this.level1Con2 = level1Con2;
		}
	}
}
