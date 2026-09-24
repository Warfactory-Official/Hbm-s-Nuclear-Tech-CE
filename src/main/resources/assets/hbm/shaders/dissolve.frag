#version 120

uniform sampler2D mainTex;
uniform sampler2D noise;
uniform sampler2D lightmap;
uniform float amount;
uniform float worldTime;
uniform vec2 noiseScroll;

varying vec2 texCoord;
varying vec2 lightmapTexCoord;
varying vec3 lightSum;

float remap(float num, float min1, float max1, float min2, float max2){
	return ((num - min1) / (max1 - min1)) * (max2 - min2) + min2;
}

void main(){
	vec4 sum = texture2D(mainTex, texCoord);
	vec4 lmap = texture2D(lightmap, lightmapTexCoord);
	vec4 noiseSample = texture2D(noise, texCoord + noiseScroll * worldTime);
	float a = noiseSample.r;
	a = a - 1 + amount * 2;
	a = remap(a, 0, 1, -10, 10);
	gl_FragColor = vec4(sum.rgb * lightSum, a) * lmap * gl_Color;
}
