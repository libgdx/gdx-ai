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

/** Utility class to generate random dungeons.
 * 
 * @author davebaol */
public final class DungeonUtils {

	public static void main (String[] args) {
		int mapSizeX = 60;
		int mapSizeY = 100;
		int map[][] = DungeonUtils.generate(mapSizeX, mapSizeY, MathUtils.random(70, 120), 3, 15);
		System.out.println(mapToString(map));
	}

	static final int TILE_EMPTY = 0;
	static final int TILE_FLOOR = 1;
	static final int TILE_WALL = 2;

	private DungeonUtils () {
	}

	public static int[][] generate (int mapSizeX, int mapSizeY, int roomCount, int roomMinSize, int roomMaxSize) {
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
		for (int k = 0; k < 100; k++) {
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

		// Iterates through all the tiles in the map and if it finds a tile that is a FLOOR
		// we check all the surrounding tiles for empty values. If we find an empty tile (that
		// touches the floor) we build a WALL.
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

		return map;
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
		float closest_distance = 1000;
		for (int i = 0; i < rooms.size; i++) {
			Room check = rooms.get(i);
			if (check == room) continue;
			float check_midX = check.x + (check.w / 2f);
			float check_midY = check.y + (check.h / 2f);
			float distance = Math.min(Math.abs(midX - check_midX) - (room.w / 2f) - (check.w / 2f), Math.abs(midY - check_midY)
				- (room.h / 2f) - (check.h / 2f));
			if (distance < closest_distance) {
				closest_distance = distance;
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

}
