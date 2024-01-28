# Java_3D_Engine

EDIT MANY YEARS LATER: this was a personal project I did when I was bored as a student during covid(2020? not a school project though) with no prior knowledge about computer graphics or software optimisation. I updated it with quality of life improvements but otherwise kept it as it was, I'm not planning on working on it again. If I did, I'd probably start all over again.

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
	p: change light point
	o: change debug mode
	i: show depth buffer
	ESC: KEYS MENU (and locks mouse)

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

EXAMPLES OF RENDER

![Exemple of render](https://github.com/yrouxel/Java_Rasterizer/blob/main/renders/holographic%20bike.png?raw=true)

![Exemple of render](https://github.com/yrouxel/Java_Rasterizer/blob/main/renders/good%20looking%20shadows.png?raw=true)