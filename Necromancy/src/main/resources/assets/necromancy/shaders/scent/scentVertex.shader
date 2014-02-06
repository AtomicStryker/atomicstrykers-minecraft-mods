varying vec4 vertColor;

void main(){
    gl_Position = projectionMatrix * modelViewMatrix * vec4( position, 1.0 );
    vertColor = vec4(0.6*noise1(5), 0.3, 0.4, 0.1);
}