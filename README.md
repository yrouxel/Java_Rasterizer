# Java_3D_Engine

An attempt to build a mono-thread optimization for a multi-thread 3D Engine

CONTROLS:

	MOVEMENT:
	z: forward
	s: backward
	q: left
	d: right
	r: up
	f: down
	t: speed up
	g: speed down

	DEBUG:
	p: change view point
	o: change debug mode
	i: show depth buffer
	u: unlock mouse

	DEBUG MODES:
	0: no additional info
	1: show all chunks with given chunk level
	2: show all chunks in chunks of given chunk level + 1
	3: precise navigation through chunks (use with keys v, b, n)

	CHUNK LEVEL:
	y: chunk level up
	h: chunk level down

	DEBUG MODE 3 SPECIAL KEYS:
	n: next chunk
	b: enter chunk
	v: back to root