#version 120

varying vec2 texCoord;
varying vec2 lightCoord;
varying vec4 color;
varying vec3 lighting;

uniform sampler2D mainTex;
uniform sampler2D lightmap;
uniform sampler2D flash_map;

void main(){
	vec4 col = color * texture2D(mainTex, texCoord) * texture2D(lightmap, lightCoord);
	vec4 fl = texture2D(flash_map, texCoord);
	gl_FragColor = vec4(col.rgb * lighting + (pow(fl.rgb, vec3(1.3))-0.1)*vec3(1, 0.72, 0.46), col.a);
}
